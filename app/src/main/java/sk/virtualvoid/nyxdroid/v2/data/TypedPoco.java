package sk.virtualvoid.nyxdroid.v2.data;

/**
 * 
 * @author Juraj
 * 
 */
public class TypedPoco<TPoco extends BasePoco> extends BasePoco {
	public enum Type {
		NONE, REPLY, ATTACHMENT
	}

	public TPoco ChildPoco;
	public Type Type;

	public TypedPoco() {
		Type = Type.NONE;
	}
	
	public TypedPoco(Type type, TPoco childPoco) {
		this.Type = type;
		this.ChildPoco = childPoco;
	}
}
