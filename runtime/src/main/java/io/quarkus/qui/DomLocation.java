package io.quarkus.qui;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represent a path in bytecode that creates a view.
 * A view is uniquely identified by this path.
 */
public final class DomLocation {
    final DomLocation parent;
    final Object key;
    final List<BytecodeLocation> bytecodeLocation;

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
                        .dropWhile(stackFrame -> !View.class.getName().equals(stackFrame.getClassName()))
                        .skip(1)
                        .takeWhile(stackFrame ->
                            callerClass.getName().equals(stackFrame.getClassName()) &&
                                "render".equals(stackFrame.getMethodName())
                        )
                        .map(BytecodeLocation::new)
                        .collect(Collectors.toList()));
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
        return Objects.equals(parent, that.parent) && Objects.equals(key, that.key) && bytecodeLocation.equals(that.bytecodeLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, key, bytecodeLocation);
    }

    @Override
    public String toString() {
        String prefix = (parent != null)? parent.toString() : " < ";
        String suffix = (key != null)? "[" + key + "]" : "";
        return bytecodeLocation.stream()
                .map(bytecodeLocation -> bytecodeLocation.className + ":" +
                        bytecodeLocation.methodName + ":" +
                        bytecodeLocation.bytecodeLocationInClassFile)
                .collect(Collectors.joining(" < ", prefix, suffix));
    }

    private static final class BytecodeLocation {
        private final String className;
        private final String methodName;
        private final int bytecodeLocationInClassFile;

        public BytecodeLocation(StackWalker.StackFrame stackFrame) {
            this.className = stackFrame.getClassName();
            this.methodName = stackFrame.getMethodName();
            this.bytecodeLocationInClassFile = stackFrame.getByteCodeIndex();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            BytecodeLocation that = (BytecodeLocation) o;
            return bytecodeLocationInClassFile == that.bytecodeLocationInClassFile && className.equals(that.className) && methodName.equals(that.methodName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(className, methodName, bytecodeLocationInClassFile);
        }
    }
}
