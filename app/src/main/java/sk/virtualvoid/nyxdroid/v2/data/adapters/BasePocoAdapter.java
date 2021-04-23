package sk.virtualvoid.nyxdroid.v2.data.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import sk.virtualvoid.nyxdroid.v2.data.BasePoco;
import sk.virtualvoid.nyxdroid.v2.internal.Appearance;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 
 * @author juraj
 * 
 * @param <T>
 */
public abstract class BasePocoAdapter<T extends BasePoco> extends BaseAdapter {

	protected AppCompatActivity context;

	protected ArrayList<T> model;
	protected Appearance appearance;

	public BasePocoAdapter(AppCompatActivity context) {
		this.context = context;
		this.model = new ArrayList<T>();
		this.appearance = Appearance.getAppearance(context);
	}
	
	public BasePocoAdapter(AppCompatActivity context, ArrayList<T> model) {
		this.context = context;
		this.model = model;
		this.appearance = Appearance.getAppearance(context);
	}
	
	public int getCount() {
		return model.size();
	}

	public ArrayList<T> getItems() {
		return model;
	}
	
	public Object getItem(int pos) {
		return model.get(pos);
	}

	public long getItemId(int pos) {
		BasePoco basePoco = model.get(pos);
		return basePoco.Id != null ? basePoco.Id : 0;
	}
	
	public int getItemPosition(long id) {
		for (int pos = 0; pos < model.size(); pos++) {
			T item = model.get(pos);
			if (item.Id == id) {
				return pos;
			}
		}
		return -1;
	}

	public T getItemById(long id) {
		for (int pos = 0; pos < model.size(); pos++) {
			T item = model.get(pos);
			if (item.Id == id) {
				return item;
			}
		}
		return null;
	}
	
	public void clearAll() {
		model.clear();
	}

	public void addItems(Collection<? extends T> items) {
		model.addAll(items);
	}
	
	public void removeItem(T item) {
		model.remove(item);
	}

	public void replaceItem(int position, T newItem) {
		model.remove(position);
		model.add(position, newItem);
	}

	public T getLastItem() {
		if (model == null || model.size() == 0) {
			return null;
		}
		return model.get(model.size() - 1);
	}

	public List<T> filter(Collection<? extends T> items) {
		ArrayList<T> list = new ArrayList<T>();
		for (T item : items) {
			if (exists(item.Id)) {
				continue;
			}
			list.add(item);
		}
		return list;
	}

	public boolean exists(long id) {
		for (T item : model) {
			if (item.Id == id) {
				return true;
			}
		}
		return false;
	}
}
