/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.util.undo;

import java.awt.Point;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.pepsoft.util.MemoryUtils;

/**
 *
 * @author pepijn
 */
public class UndoManager {
    public UndoManager() {
        this(null, null, DEFAULT_MAX_FRAMES);
    }

    public UndoManager(int maxFrames) {
        this(null, null, maxFrames);
    }
    
    public UndoManager(Action undoAction, Action redoAction) {
        this(undoAction, redoAction, DEFAULT_MAX_FRAMES);
    }

    public UndoManager(Action undoAction, Action redoAction, int maxFrames) {
        this.undoAction = undoAction;
        this.redoAction = redoAction;
        this.maxFrames = maxFrames;
        history.add(new WeakHashMap<BufferKey<?>, Object>());
        updateActions();
    }
    
    public int getMaxFrames() {
        return maxFrames;
    }

    /**
     * Arm a save point. It will be executed the next time a buffer is requested
     * for editing. Arming a save point instead of executing it immediately
     * allows a redo to be performed instead.
     * 
     * <p>Will do nothing if a save point is already armed, or if the current
     * frame is the last one and it is not dirty.
     */
    public void armSavePoint() {
        if ((! savePointArmed) /*&& ((currentFrame < (history.size() - 1)) || isDirty())*/) {
            savePointArmed = true;
            for (UndoListener listener: listeners) {
                listener.savePointArmed();
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Save point armed");
            }
        }
    }

    /**
     * Save the current state of all buffers as an undo point.
     */
    public void savePoint() {
        clearRedo();

        // Add a new frame
        history.add(new WeakHashMap<BufferKey<?>, Object>());
        
        // Update the current frame pointer
        currentFrame++;

        // If the max undos has been reached, throw away the oldest
        pruneHistory();

        // Clear cache
        writeableBufferCache.clear();

        savePointArmed = false;

        for (UndoListener listener: listeners) {
            listener.savePointCreated();
        }
        
        updateActions();

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Save point set; new current frame: " + currentFrame);
            if (logger.isLoggable(Level.FINER)) {
                dumpBuffer();
            }
        }
    }
    
    /**
     * Get a read-only snapshot of the current state of the buffers. If you want
     * the state to be a static snapshot that will not reflect later changes,
     * you should execute a save point after getting the snapshot. The snapshot
     * will remain valid until the corresponding undo history frame disappears,
     * after which it will throw an exception if you try to use it.
     * 
     * @return A snapshot of the current undo history frame.
     */
    public Snapshot getSnapshot() {
        Snapshot snapshot = new Snapshot(this, currentFrame);
        snapshots.add(new WeakReference<Snapshot>(snapshot));
        return snapshot;
    }

    /**
     * Indicates whether the current history frame is dirty.
     * 
     * @return <code>true</code> if the current history frame is dirty.
     */
    public boolean isDirty() {
        return ! writeableBufferCache.isEmpty();
    }

    /**
     * Rolls back all buffers to the previous save point, if there is one still
     * available.
     *
     * @return <code>true</code> if the undo was succesful.
     */
    public boolean undo() {
        if (currentFrame > 0) {
            currentFrame--;
            readOnlyBufferCache.clear();
            writeableBufferCache.clear();
            for (UndoListener listener: listeners) {
                listener.undoPerformed();
            }
            Map<BufferKey<?>, Object> previousHistoryFrame = history.get(currentFrame + 1);
            for (BufferKey<?> key: previousHistoryFrame.keySet()) {
                UndoListener listener = keyListeners.get(key);
                if (listener != null) {
                    listener.bufferChanged(key);
                }
            }
            updateActions();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Undo requested; now at frame " + currentFrame + " (total: " + history.size() + ")");
                if (logger.isLoggable(Level.FINER)) {
                    dumpBuffer();
                }
            }
            return true;
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Undo requested, but no more frames available");
            }
            return false;
        }
    }

    /**
     * Rolls forward all buffers to the next save point, if there is one
     * available, and no edits have been performed since the last undo.
     *
     * @return <code>true</code> if the redo was succesful.
     */
    public boolean redo() {
        if (currentFrame < (history.size() - 1)) {
            currentFrame++;
            readOnlyBufferCache.clear();
            writeableBufferCache.clear();
            for (UndoListener listener: listeners) {
                listener.redoPerformed();
            }
            Map<BufferKey<?>, Object> currentHistoryFrame = history.get(currentFrame);
            for (BufferKey<?> key: currentHistoryFrame.keySet()) {
                UndoListener listener = keyListeners.get(key);
                if (listener != null) {
                    listener.bufferChanged(key);
                }
            }
            updateActions();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Redo requested; now at frame " + currentFrame + " (total: " + history.size() + ")");
                if (logger.isLoggable(Level.FINER)) {
                    dumpBuffer();
                }
            }
            return true;
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Redo requested, but no more frames available");
            }
            return false;
        }
    }

    /**
     * Throw away all undo and redo information.
     */
    public void clear() {
        clearRedo();
        
        int deletedFrames = 0;
        while (history.size() > 1) {
            shrinkHistory();
            deletedFrames++;
        }
        updateSnapshots(-deletedFrames);
        updateActions();
        savePointArmed = false;
        if (logger.isLoggable(Level.FINER)) {
            dumpBuffer();
        }
    }

    /**
     * Throw away all redo information
     */
    public void clearRedo() {
        // Make sure there is no history after the current frame (which there
        // might be if an undo has been performed)
        if (currentFrame < (history.size() - 1)) {
            do {
                history.removeLast();
            } while (currentFrame < (history.size() - 1));
            updateSnapshots(0);
            updateActions();
        }
    }

    public <T> void addBuffer(BufferKey<T> key, T buffer) {
        addBuffer(key, buffer, null);
    }

    public <T> void addBuffer(BufferKey<T> key, T buffer, UndoListener listener) {
        clearRedo();
        
        history.getLast().put(key, buffer);
        writeableBufferCache.put(key, buffer);
        if (listener != null) {
            keyListeners.put(key, listener);
        }
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Buffer added: " + key);
        }
    }

    public void removeBuffer(BufferKey<?> key) {
        writeableBufferCache.remove(key);
        readOnlyBufferCache.remove(key);
        for (Map<BufferKey<?>, Object> historyFrame: history) {
            historyFrame.remove(key);
        }
        keyListeners.remove(key);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Buffer removed: " + key);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getBuffer(BufferKey<T> key) {
        if (writeableBufferCache.containsKey(key)) {
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("Getting buffer " + key + " for reading from writeable buffer cache");
            }
            return (T) writeableBufferCache.get(key);
        } else if (readOnlyBufferCache.containsKey(key)) {
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("Getting buffer " + key + " for reading from read-only buffer cache");
            }
            return (T) readOnlyBufferCache.get(key);
        } else {
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("Getting buffer " + key + " for reading from history");
            }
            T buffer = findMostRecentCopy(key);
            readOnlyBufferCache.put(key, buffer);
            return buffer;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getBufferForEditing(BufferKey<T> key) {
        if (savePointArmed) {
            savePoint();
        }
        if (writeableBufferCache.containsKey(key)) {
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("Getting buffer " + key + " for writing from writeable buffer cache");
            }
            return (T) writeableBufferCache.get(key);
        } else {
            clearRedo();
            if (readOnlyBufferCache.containsKey(key)) {
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("Copying buffer " + key + " for writing from read-only buffer cache");
                }
                T buffer = (T) readOnlyBufferCache.remove(key);
                T copy = copyObject(buffer);
                history.getLast().put(key, copy);
                writeableBufferCache.put(key, copy);
                return copy;
            } else {
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("Copying buffer " + key + " for writing from history");
                }
                Map<BufferKey<?>, Object> currentHistoryFrame = history.getLast();
                if (currentHistoryFrame.containsKey(key)) {
                    // TODO: this should never happen. Remove?
                    T buffer = (T) currentHistoryFrame.get(key);
                    writeableBufferCache.put(key, buffer);
                    return buffer;
                } else {
                    // The buffer does not exist in the current history frame yet. Copy
                    // it.
                    T buffer = findMostRecentCopy(key);
                    T copy = copyObject(buffer);
                    currentHistoryFrame.put(key, copy);
                    writeableBufferCache.put(key, copy);
                    return copy;
                }
            }
        }
    }

    public void addListener(UndoListener listener) {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Adding listener " + listener);
        }
        listeners.add(listener);
    }

    public void removeListener(UndoListener listener) {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Removing listener " + listener);
        }
        listeners.remove(listener);
    }

    public Class<?>[] getStopAtClasses() {
        return stopAt.toArray(new Class<?>[stopAt.size()]);
    }

    public void setStopAtClasses(Class<?>... stopAt) {
        this.stopAt = new HashSet<Class<?>>(Arrays.asList(stopAt));
    }
    
    public int getDataSize() {
        return MemoryUtils.getSize(history, stopAt);
    }
    
    private void updateSnapshots(int delta) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Updating snapshots");
        }
        int frameCount = history.size();
        for (Iterator<Reference<Snapshot>> i = snapshots.iterator(); i.hasNext(); ) {
            Snapshot snapshot = i.next().get();
            if (snapshot == null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Removing garbage collected snapshot");
                }
                i.remove();
            } else {
                snapshot.frame += delta;
                if ((snapshot.frame < 0) || (snapshot.frame >= frameCount)) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Disabling and removing snapshot with invalid frame reference");
                    }
                    snapshot.frame = -1;
                    i.remove();
                }
            }
        }
    }
    
    private void pruneHistory() {
        int deletedFrames = 0;
        while (history.size() > maxFrames) {
            shrinkHistory();
            deletedFrames++;
        }
        if (deletedFrames > 0) {
            updateSnapshots(-deletedFrames);
        }
        if (logger.isLoggable(Level.FINER)) {
            dumpBuffer();
        }
    }
    
    private void shrinkHistory() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Removing oldest history frame; moving contents to next oldest frame");
        }
        
        // Remove oldest frame
        Map<BufferKey<?>, Object> oldestFrame = history.removeFirst();

        // Move all buffers from the previous oldest frame to the new
        // oldest frame, except the ones that already exist
        Map<BufferKey<?>, Object> nextOldestFrame = history.getFirst();
        for (Map.Entry<BufferKey<?>, Object> entry: oldestFrame.entrySet()) {
            if (! nextOldestFrame.containsKey(entry.getKey())) {
                nextOldestFrame.put(entry.getKey(), entry.getValue());
            }
        }
        
        if (currentFrame > 0) {
            currentFrame--;
        }
    }

    private <T> T findMostRecentCopy(BufferKey<T> key) {
        return findMostRecentCopy(key, currentFrame);
    }
    
    @SuppressWarnings("unchecked")
    <T> T findMostRecentCopy(BufferKey<T> key, int frame) {
        for (ListIterator<Map<BufferKey<?>, Object>> i = history.listIterator(frame + 1); i.hasPrevious(); ) {
            Map<BufferKey<?>, Object> historyFrame = i.previous();
            if (historyFrame.containsKey(key)) {
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("Most recent copy of buffer " + key + " found in frame " + frame + " of history");
                }
                return (T) historyFrame.get(key);
            }
            frame--;
        }
        throw new IllegalStateException("No buffer exists for key " + key);
    }

    /**
     * Make a deep copy of an object. Only a restricted set of types is
     * supported. Will automatically throw away redo and/or undo information if
     * there is not enough memory, until there is no more information to throw
     * away, in which case it will throw an <code>OutOfMemoryError</code>.
     * 
     * @param <T> The type of the object.
     * @param object The object to copy.
     * @return A deep copy of the object.
     * @throws OutOfMemoryError If there is not enough memory to copy the
     *     object, after throwing away all redo and undo information.
     */
    @SuppressWarnings("unchecked")
    private <T> T copyObject(T object) {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Copying object of type " + object.getClass().getName());
        }
        while (true) {
            try {
                if (object instanceof BitSet) {
                    return (T) ((BitSet) object).clone();
                } else if (object instanceof byte[]) {
                    return (T) ((byte[]) object).clone();
                } else if (object instanceof short[]) {
                    return (T) ((short[]) object).clone();
                } else if (object instanceof int[]) {
                    return (T) ((int[]) object).clone();
                } else if (object instanceof Map) {
                    Map<Object, Object> copy;
                    if (object instanceof SortedMap) {
                        copy = new TreeMap<Object, Object>();
                    } else {
                        copy = new HashMap<Object, Object>();
                    }
                    boolean first = true, deeplyCopyKeys = false;
                    for (Map.Entry entry: ((Map<?, ?>) object).entrySet()) {
                        if (first) {
                            deeplyCopyKeys = entry.getKey() instanceof DeeplyCopyable;
                            first = false;
                        }
                        copy.put(deeplyCopyKeys ? copyObject(entry.getKey()) : entry.getKey(), copyObject(entry.getValue()));
                    }
                    return (T) copy;
                } else if (object instanceof List) {
                    List<Object> copy;
                    if (object instanceof RandomAccess) {
                        copy = new ArrayList<Object>(((List) object).size());
                    } else {
                        copy = new LinkedList<Object>();
                    }
                    for (Object entry: (List) object) {
                        copy.add(copyObject(entry));
                    }
                    return (T) copy;
                } else if (object instanceof Set) {
                    Set<Object> copy;
                    if (object instanceof SortedSet) {
                        copy = new TreeSet<Object>();
                    } else {
                        copy = new HashSet<Object>();
                    }
                    for (Object entry: (Set) object) {
                        copy.add(copyObject(entry));
                    }
                    return (T) copy;
                } else if (IMMUTABLE_TYPES.contains(object.getClass())) {
                    return object;
                } else if (object instanceof DeeplyCopyable) {
                    return ((DeeplyCopyable<T>) object).deepCopy();
                } else if (object instanceof Cloneable) {
                    return ((Cloneable<T>) object).clone();
                } else {
                    throw new UnsupportedOperationException("Don't know how to copy a " + object.getClass());
                }
            } catch (OutOfMemoryError e) {
                if (trimBufferOnOOME && (currentFrame < (history.size() - 1))) {
                    logger.info("Not enough memory to copy buffer; deleting redo information");
                    clearRedo();
                } else if (trimBufferOnOOME && (history.size() > 1)) {
                    logger.info("Not enough memory to copy buffer; deleting one level of undo information");
                    shrinkHistory();
                    updateSnapshots(-1);
                    updateActions();
                } else {
                    throw e;
                }
            }
        }
    }
    
    private void dumpBuffer() {
        int index = 0;
        long totalDataSize = 0;
        for (Map<BufferKey<?>, Object> frame: history) {
            int frameSize = MemoryUtils.getSize(frame, stopAt);
            totalDataSize += frameSize;
            logger.fine(((index == currentFrame) ? "* " : "  ") + " " + ((index < 10) ? "0" : "") + index + ": " + frame.size() + " buffers (size: " + (frameSize / 1024) + " KB)");
            index++;
        }
        logger.fine("   Total data size: " + (totalDataSize / 1024) + " KB");
    }
    
    private void updateActions() {
        if (undoAction != null) {
            undoAction.setEnabled(currentFrame > 0);
        }
        if (redoAction != null) {
            redoAction.setEnabled(currentFrame < (history.size() - 1));
        }
    }
 
    private final Action undoAction, redoAction;
    private final int maxFrames;
    private final LinkedList<Map<BufferKey<?>, Object>> history = new LinkedList<Map<BufferKey<?>, Object>>();
    private int currentFrame;
    private final Map<BufferKey<?>, Object> readOnlyBufferCache = new WeakHashMap<BufferKey<?>, Object>();
    private final Map<BufferKey<?>, Object> writeableBufferCache = new WeakHashMap<BufferKey<?>, Object>();
    private final List<UndoListener> listeners = new ArrayList<UndoListener>();
    private final Map<BufferKey<?>, UndoListener> keyListeners = new WeakHashMap<BufferKey<?>, UndoListener>();
    private boolean savePointArmed;
    private final Set<Reference<Snapshot>> snapshots = new HashSet<Reference<Snapshot>>();
    private final boolean trimBufferOnOOME = false;
    private Set<Class<?>> stopAt;
 
    @SuppressWarnings("unchecked") // Guaranteed by Java
    private static final Set<Class<?>> IMMUTABLE_TYPES = new HashSet<Class<?>>(Arrays.asList(Point.class));
    private static final int DEFAULT_MAX_FRAMES = 25;
    private static final Logger logger = Logger.getLogger(UndoManager.class.getName());
}