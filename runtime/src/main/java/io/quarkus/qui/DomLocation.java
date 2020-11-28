package io.quarkus.qui;

import java.util.Objects;

/**
 * Represent a path in bytecode that creates a view.
 * A view is uniquely identified by this path.
 */
public final class DomLocation {
    final DomLocation parent;
    final Object key;
    final int bytecodeLocation;

    public DomLocation(Object key, Class<?> callerClass) {
        this(null, key, callerClass);
    }

    public DomLocation(final DomLocation parent, final Object key, final Class<?> callerClass) {
        // rendering starts from Window.draw,
        // and this is called only from ViewManager
        this.parent = parent;
        this.key = key;
        this.bytecodeLocation = StackWalker.getInstance()
                .walk(stackFrameStream -> stackFrameStream
                        .filter(st -> st.getClassName().equals(callerClass.getTypeName()))
                        .findFirst()
                        .map(StackWalker.StackFrame::getByteCodeIndex)
                        .orElseThrow());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DomLocation that = (DomLocation) o;
        return bytecodeLocation == that.bytecodeLocation &&
                Objects.equals(parent, that.parent) &&
                Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, key, bytecodeLocation);
    }

    @Override
    public String toString() {
        String prefix = (parent != null)? parent.toString() : " > ";
        String suffix = (key != null)? "[" + key + "]" : "";
        return prefix + bytecodeLocation + suffix;
    }
}
