package sk.virtualvoid.core.widgets;

public interface INavigationSpan {
    String getUrl();

    Long getDiscussionId();
    Long getPostId();

    boolean isImage();
    boolean isNavigation();
}
