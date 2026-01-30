package cl.estencia.labs.muplayer.console.unixconsole.listener;

import cl.estencia.labs.muplayer.console.common.enums.InterceptorMode;
import cl.estencia.labs.muplayer.console.unixconsole.event.KeyInputEvent;
import lombok.Getter;

import static cl.estencia.labs.muplayer.console.common.enums.InterceptorMode.GLOBAL;

@Getter
public abstract class KeyInterceptor implements NativeInputListener<KeyInputEvent>,
                                                Interceptor<KeyInputEvent> {
    protected final char key;
    protected final InterceptorMode mode;

    public KeyInterceptor(Character key) {
        this(key, GLOBAL);
    }

    public KeyInterceptor(Integer key) {
        this(key, GLOBAL);
    }

    public KeyInterceptor(Character key, InterceptorMode mode) {
        if (key == null) {
            throw new NullPointerException("The interceptors key can not be null");
        }

        this.key = key;
        this.mode = mode;
    }

    public KeyInterceptor(Integer key, InterceptorMode mode) {
        if (key == null) {
            throw new NullPointerException("The interceptors key can not be null");
        }

        this.key = (char) key.intValue();
        this.mode = mode;
    }

    public boolean isKey(int key) {
        return this.key == key;
    }

    public boolean isGlobal() {
        return mode == GLOBAL;
    }

    @Override
    public void onInputEvent(KeyInputEvent event) {
        if (!isKey(event.getKey())) {
            return;
        }

        intercept(event);
    }

}
