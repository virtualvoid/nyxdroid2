package sk.virtualvoid.nyxdroid.v2.internal;

/**
 * @author Juraj
 */
public enum VotingType {
    POSITIVE("positive"),
    NEGATIVE("negative"),
    NEGATIVE_VISIBLE("negative_visible"),
    REMOVE("remove");

    private String value;

    private VotingType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public boolean isRemove() {
        return this.value.equals(REMOVE.toString());
    }
}
