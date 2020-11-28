package io.quarkus.qui;

public class TestView implements View<TestView.TestViewProps> {

    interface TestViewProps extends Props<TestViewProps> {
        TestViewProps age(int x);
    }

    @Override
    public Props<?> render(TestViewProps props) {
        var x = get(props::age);
        return null;
    }

}
