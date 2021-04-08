package sk.virtualvoid.nyxdroid.v2.internal;

/**
 * @author Juraj
 */
public enum VotingType {
    POSITIVE("positive"),
    NEGATIVE("negative"),
    NONE("");

    private String value;

    private VotingType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
