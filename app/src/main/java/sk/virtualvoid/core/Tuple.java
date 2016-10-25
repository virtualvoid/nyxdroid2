package sk.virtualvoid.core;

/**
 * 
 * @author Juraj
 * 
 */
public class Tuple<TFirst, TSecond> {
	public TFirst First;
	public TSecond Second;

	public Tuple() {

	}

	public Tuple(TFirst first, TSecond second) {
		this();

		this.First = first;
		this.Second = second;
	}
}
