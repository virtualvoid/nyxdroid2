package sk.virtualvoid.nyxdroid.v2;

import java.util.ArrayList;

import sk.virtualvoid.core.DialogDecorator;
import sk.virtualvoid.core.ImageDownloader;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.nyxdroid.v2.data.BasePoco;
import sk.virtualvoid.nyxdroid.v2.data.dac.WriteupDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.query.WriteupQuery;
import sk.virtualvoid.nyxdroid.v2.internal.VotingInfoResult;
import sk.virtualvoid.nyxdroid.v2.internal.VotingResponse;
import sk.virtualvoid.nyxdroid.v2.internal.VotingType;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 
 * @author Juraj
 * 
 */
public class WriteupRatingsDialog extends Dialog {
	private long discussionId;
	private long wuId;
	private VotingInfoResult lastVotingInfoResult;
	
	private Activity context;
	
	private TextView positiveRatingCount;
	private ListView positiveRatings;
	private TextView negativeRatingCount;
	private ListView negativeRatings;
	private Button cancelMyVoteButton;
	
	private ImageDownloader imageDownloader;

	public WriteupRatingsDialog(final Activity context, long discussionId, long wuId) {
		super(context);

		DialogDecorator.decorate(this, 0.9, 0.8, true);

		setContentView(R.layout.writeup_rating_overview);
		setTitle(context.getString(R.string.hodnocen_));

		Drawable emptyAvatar = context.getResources().getDrawable(R.drawable.empty_avatar);

		imageDownloader = new ImageDownloader(context, emptyAvatar);

		positiveRatingCount = (TextView) findViewById(R.id.ratings_positive_count);
		positiveRatings = (ListView) findViewById(R.id.ratings_listview_positive);
		positiveRatings.setDividerHeight(0);
		positiveRatings.setDivider(null);

		negativeRatingCount = (TextView) findViewById(R.id.ratings_negative_count);
		negativeRatings = (ListView) findViewById(R.id.ratings_listview_negative);
		negativeRatings.setDividerHeight(0);
		negativeRatings.setDivider(null);
		
		cancelMyVoteButton = (Button)findViewById(R.id.ratings_myvote_toggle);
		cancelMyVoteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cancelMyVote();
				
				// zrusit ho mozeme len raz, ziadny toggle nebude
				cancelMyVoteButton.setEnabled(false);
			}
		});
		
		this.context = context;
		this.discussionId = discussionId;
		this.wuId = wuId;
		
		refresh();
	}

	private void refresh() {
		WriteupQuery query = new WriteupQuery();
		query.Id = discussionId;
		query.TempId = wuId;

		Task<WriteupQuery, VotingInfoResult> task = WriteupDataAccess.getRatingInfo(context, new RatingInfoTaskListener());
		TaskManager.startTask(task, query);
	}
	
	private void cancelMyVote() {
		if (lastVotingInfoResult == null) {
			// seriously, wtf
			return;
		}
		
		WriteupQuery query = new WriteupQuery();
		query.Id = discussionId;
		query.TempId = wuId;
		query.VoteToggle = true;
		
		if (lastVotingInfoResult.MeVotedNegative) {
			query.VotingType = VotingType.NEGATIVE;
		}
		
		if (lastVotingInfoResult.MeVotedPositive) {
			query.VotingType = VotingType.POSITIVE;
		}
		
		Task<WriteupQuery, VotingResponse> task = WriteupDataAccess.giveRating(context, new RatingGiveTaskListener());
		TaskManager.startTask(task, query);
	}
	
	private synchronized ImageDownloader getImageDownloader() {
		return imageDownloader;
	}

	class RatingGiveTaskListener extends TaskListener<VotingResponse> {
		@Override
		public void done(VotingResponse output) {
			// TODO: kontrola ?
			refresh();
		}
	}
	
	/**
	 * 
	 * @author Juraj
	 * 
	 */
	class RatingInfoTaskListener extends TaskListener<VotingInfoResult> {
		@Override
		public void done(VotingInfoResult output) {
			lastVotingInfoResult = output;
			
			if (lastVotingInfoResult.Positive != 0) {
				positiveRatingCount.setText(String.format("+%d", lastVotingInfoResult.Positive));
				positiveRatings.setAdapter(new RatingAdapter((Activity) getContext(), lastVotingInfoResult.PositiveList));
			} else {
				positiveRatingCount.setText(R.string.no_one_at_this_time);
			}

			if (lastVotingInfoResult.Negative != 0) {
				negativeRatingCount.setText(String.format("-%d", lastVotingInfoResult.Negative));
				negativeRatings.setAdapter(new RatingAdapter((Activity) getContext(), lastVotingInfoResult.NegativeList));
			} else {
				negativeRatingCount.setText(R.string.no_one_at_this_time);
			}
			
			if ((lastVotingInfoResult.MeVotedNegative || lastVotingInfoResult.MeVotedPositive) && cancelMyVoteButton != null) {
				cancelMyVoteButton.setEnabled(true);
			}
		}
	}

	/**
	 * 
	 * @author Juraj
	 * 
	 */
	class RatingAdapter extends BaseAdapter {
		private Activity context;
		private ArrayList<String> model;

		public RatingAdapter(Activity context, ArrayList<String> model) {
			this.context = context;
			this.model = model;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			ViewHolder holder = null;

			if (row == null) {
				holder = new ViewHolder();

				row = context.getLayoutInflater().inflate(R.layout.writeup_rating_overview_row, parent, false);
				holder.Thumbnail = (ImageView) row.findViewById(R.id.writeup_rating_overview_row_thumbnail);
				holder.Nick = (TextView) row.findViewById(R.id.writeup_rating_overview_row_nick);

				row.setTag(holder);
			} else {
				holder = (ViewHolder) row.getTag();
			}

			String nick = (String) getItem(position);

			getImageDownloader().download(BasePoco.nickToUrl(nick), holder.Thumbnail);
			holder.Nick.setText(nick);

			return row;
		}

		@Override
		public int getCount() {
			return model.size();
		}

		@Override
		public Object getItem(int position) {
			return model.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
	}

	static class ViewHolder {
		public ImageView Thumbnail;
		public TextView Nick;
	}
}
