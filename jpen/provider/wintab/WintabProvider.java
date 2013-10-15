/* [{
Copyright 2007, 2008 Nicolas Carranza <nicarran at gmail.com>

This file is part of jpen.

jpen is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License,
or (at your option) any later version.

jpen is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with jpen.  If not, see <http://www.gnu.org/licenses/>.
}] */
package jpen.provider.wintab;

import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.InputEvent;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import jpen.internal.BuildInfo;
import jpen.internal.ObjectUtils;
import jpen.internal.Range;
import jpen.PenManager;
import jpen.PenProvider;
import jpen.PLevel;
import jpen.provider.AbstractPenProvider;
import jpen.provider.NativeLibraryLoader;
import jpen.provider.VirtualScreenBounds;

public class WintabProvider
	extends AbstractPenProvider {
	private static final Logger L=Logger.getLogger(WintabProvider.class.getName());
	public static final int PERIOD=10;
	private static final NativeLibraryLoader LIB_LOADER=new NativeLibraryLoader(new String[]{""},
			new String[]{"64"},
			Integer.valueOf(BuildInfo.getProperties().getString("jpen.provider.wintab.nativeVersion")));
	//static{L.setLevel(Level.ALL);}

	static void loadLibrary(){
		LIB_LOADER.load();
	}

	public final WintabAccess wintabAccess;
	private final Map<Integer, WintabDevice> cursorToDevice=new HashMap<Integer, WintabDevice>();
	private final Range[] levelRanges=new Range[PLevel.Type.VALUES.size()];
	final VirtualScreenBounds screenBounds=VirtualScreenBounds.getInstance();
	private final Thread thread;
	private volatile boolean paused=true;
	private boolean systemCursorEnabled=true; // by default the tablet device moves the system pointer (cursor)

	public static class Constructor
		extends AbstractPenProvider.AbstractConstructor{
		//@Override
		public String getName() {
			return "Wintab";
		}
		//@Override
		public boolean constructable(PenManager penManager) {
			return System.getProperty("os.name").toLowerCase().contains("windows");
		}

		@Override
		public PenProvider constructProvider() throws Throwable {
			loadLibrary();
			WintabAccess wintabAccess=new WintabAccess();
			return new WintabProvider(this, wintabAccess);
		}
		@Override
		public int getNativeVersion(){
			return LIB_LOADER.nativeVersion;
		}
		@Override
		public int getNativeBuild(){
			loadLibrary();
			return WintabAccess.getNativeBuild();
		}
		@Override
		public int getExpectedNativeBuild(){
			return Integer.valueOf(BuildInfo.getProperties().getString("jpen.provider.wintab.nativeBuild"));
		}
	}

	/**
		When this system property is set to true then the "AWT firing mode" is used. The AWT firing mode is currently EXPERIMENTAL and may be removed in a future release.
		*/
	private static final String AWT_EVENT_FIRING_MODE_SYS_PROPERTY="jpen.wintabProvider.awtEventFiringMode";
	private static final int AWT_EVENT_FIRING_MODE_PERIOD=1000/50;
	private static final boolean AWT_EVENT_FIRING_MODE=Boolean.valueOf(
				System.getProperty(AWT_EVENT_FIRING_MODE_SYS_PROPERTY));
	static{
		if(AWT_EVENT_FIRING_MODE)
			L.info("AWT_EVENT_FIRING_MODE enabled");
	}

	class MyThread
		extends Thread implements AWTEventListener{

		private long scheduleTime;
		private long awtEventTime;
		private int inputEventModifiers;
		private boolean awtSleep;
		private final Object awtLock=new Object();

		{
			setName("jpen-WintabProvider");
			setDaemon(true);
			setPriority(Thread.MAX_PRIORITY);
			if(AWT_EVENT_FIRING_MODE)
				Toolkit.getDefaultToolkit().addAWTEventListener(this, ~0);
		}

		public void run() {
			try{
				KeyboardFocusManager keyboardFocusManager=KeyboardFocusManager.getCurrentKeyboardFocusManager();
				boolean awtSchedule=false;
				while(true) {
					if(AWT_EVENT_FIRING_MODE){
						if(awtEventTime>scheduleTime){
							if(awtSchedule)
								schedule();
							else if(awtEventTime-scheduleTime>=AWT_EVENT_FIRING_MODE_PERIOD)
								schedule();
							awtSchedule=true;
						}else{
							schedule();
							awtSchedule=false;
						}
					}
					else
						processQueuedEvents();
					synchronized(this){
						if(AWT_EVENT_FIRING_MODE){
							/*
							if(System.currentTimeMillis()-awtEventTime>1000 &&
								 (inputEventModifiers==0 || keyboardFocusManager.getActiveWindow()==null)){
								awtSleep=true;
								wait(500);
								awtSleep=false;
								}else
								wait(AWT_FIRING_MODE_PERIOD);
							*/
							/*
							wait(System.currentTimeMillis()-awtEventTime>1000 &&
									 (inputEventModifiers==0 || keyboardFocusManager.getActiveWindow()==null)?
									 500:
									 AWT_EVENT_FIRING_MODE_PERIOD);
									 */
							wait(scheduleTime-awtEventTime>1000 &&
									 (inputEventModifiers==0 || keyboardFocusManager.getActiveWindow()==null)?
									 500:
									 AWT_EVENT_FIRING_MODE_PERIOD);
						}else
							wait(PERIOD);
						while(paused){
							L.fine("going to wait...");
							wait();
							L.fine("notified");
						}
					}
				}
			}catch(InterruptedException ex){
				throw new AssertionError(ex);
			}
		}

		private void schedule(){
			scheduleTime=System.currentTimeMillis();
			processQueuedEvents();
		}
		//@Override
		public synchronized void eventDispatched(AWTEvent ev){
			InputEvent inputEvent=ev instanceof InputEvent? (InputEvent)ev: null;
			synchronized(this){
				awtEventTime=System.currentTimeMillis();
				if(inputEvent!=null)
					inputEventModifiers=inputEvent.getModifiersEx();
				//if(awtSleep)
				if(!paused)
					notify();
			}
		}
	}

	private WintabProvider(Constructor constructor, WintabAccess wintabAccess) {
		super(constructor);
		L.fine("start");
		this.wintabAccess=wintabAccess;

		for(int i=PLevel.Type.VALUES.size(); --i>=0;){
			PLevel.Type levelType=PLevel.Type.VALUES.get(i);
			levelRanges[levelType.ordinal()]=wintabAccess.getLevelRange(levelType);
		}

		thread=new MyThread();
		thread.start();
		L.fine("end");
	}

	Range getLevelRange(PLevel.Type type) {
		return levelRanges[type.ordinal()];
	}

	private void processQueuedEvents() {
		L.finer("start");
		while(wintabAccess.nextPacket() && !paused) {
			WintabDevice device=getDevice(wintabAccess.getCursor());
			if(L.isLoggable(Level.FINE)){
				L.finer("device: ");
				L.finer(device.getName());
			}
			device.scheduleEvents();
		}
		L.finer("end");
	}

	private WintabDevice getDevice(int cursor) {
		WintabDevice wintabDevice=cursorToDevice.get(cursor);
		if(wintabDevice==null) {
			cursorToDevice.put(cursor, wintabDevice=new WintabDevice(this, cursor));
			devices.clear();
			devices.addAll(cursorToDevice.values());
			getPenManager().firePenDeviceAdded(getConstructor(), wintabDevice);
		}
		return wintabDevice;
	}

	//@Override
	public void penManagerPaused(boolean paused) {
		setPaused(paused);
	}

	synchronized void setPaused(boolean paused) {
		L.fine("start");
		if(paused==this.paused)
			return;
		this.paused=paused;
		if(!paused){
			L.fine("false paused value");
			screenBounds.reset();
			synchronized(thread) {
				L.fine("going to notify all...");
				thread.notifyAll();
				L.fine("done notifying ");
			}
			wintabAccess.enable(true);
		}
		L.fine("end");
	}

	@Override
	public boolean getUseRelativeLocationFilter(){
		return systemCursorEnabled;
	}

	/**
	@param systemCursorEnabled If <code>false<code> then tablet movement on Wintab devices doesn't cause movement on the system mouse pointer. If <code>true<code> then tablet movement on Wintab devices cause movement on the system mouse pointer, this is the default value. 
	*/
	public synchronized void setSystemCursorEnabled(boolean systemCursorEnabled){
		if(this.systemCursorEnabled==systemCursorEnabled)
			return;
		this.systemCursorEnabled=systemCursorEnabled;
		wintabAccess.setSystemCursorEnabled(systemCursorEnabled);
	}

	public synchronized boolean getSystemCursorEnabled(){
		return systemCursorEnabled;
	}
}