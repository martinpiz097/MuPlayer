package cl.estencia.labs.muplayer.console.unix.listener;

import cl.estencia.labs.muplayer.console.unix.event.KeyInputEvent;
import lombok.Getter;

@Getter
public abstract class KeyInterceptor implements NativeInputListener<KeyInputEvent>,
                                                Interceptor<KeyInputEvent> {
    protected final char key;

    public KeyInterceptor(Character key) {
        if (key == null) {
            throw new NullPointerException("The interceptors key can not be null");
        }

        this.key = key;
    }

    public KeyInterceptor(Integer key) {
        if (key == null) {
            throw new NullPointerException("The interceptors key can not be null");
        }

        this.key = (char) key.intValue();
    }

    public boolean isKey(int key) {
        return this.key == key;
    }

    @Override
    public void onInputEvent(KeyInputEvent event) {
        if (!isKey(event.getKey())) {
            return;
        }

        intercept(event);
    }

}
