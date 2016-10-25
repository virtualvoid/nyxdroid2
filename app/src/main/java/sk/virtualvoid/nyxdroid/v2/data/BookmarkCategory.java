package sk.virtualvoid.nyxdroid.v2.data;

/**
 * 
 * @author Juraj
 *
 */
public class BookmarkCategory extends Bookmark {
	public static final long RepliesCategoryId = -2;
	
	public BookmarkCategory() {
	}
	
	public BookmarkCategory(long id, String name) {
		this.Id = id;
		this.Name = name;
	}
}
