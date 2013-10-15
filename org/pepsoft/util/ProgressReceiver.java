package org.pepsoft.util;

public interface ProgressReceiver {
    void setProgress(float progress) throws OperationCancelled;
    void exceptionThrown(Throwable exception);
    void done();
    void setMessage(String message) throws OperationCancelled;
    void checkForCancellation() throws OperationCancelled;

    public static class OperationCancelled extends Exception {
        public OperationCancelled(String message) {
            super(message);
        }

        private static final long serialVersionUID = 1L;
    }
}