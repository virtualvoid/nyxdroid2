package sk.virtualvoid.nyxdroid.v2.data.adapters;

import java.util.ArrayList;
import java.util.Date;

import sk.virtualvoid.core.CustomHtml;
import sk.virtualvoid.core.ImageDownloader;
import sk.virtualvoid.core.ImageGetterAsync;
import sk.virtualvoid.core.NyxException;
import sk.virtualvoid.core.ResponsibleBaseAdapter;
import sk.virtualvoid.nyxdroid.v2.R;
import sk.virtualvoid.nyxdroid.v2.data.BasePoco;
import sk.virtualvoid.nyxdroid.v2.data.Poll;
import sk.virtualvoid.nyxdroid.v2.data.PollAnswer;
import sk.virtualvoid.nyxdroid.v2.data.Writeup;
import sk.virtualvoid.nyxdroid.v2.internal.IPollVotingHandler;
import sk.virtualvoid.nyxdroid.v2.internal.IWriteupSelectionHandler;
import sk.virtualvoid.nyxdroid.v2.internal.IVotingHandler;
import sk.virtualvoid.nyxdroid.v2.internal.VotingType;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @author Juraj
 */
public class WriteupAdapter extends BasePocoAdapter<Writeup> implements ResponsibleBaseAdapter {
    private ImageDownloader imageDownloader;
    private ImageGetterAsync imageGetterAsync;
    private boolean displayVotingThumbs;
    private boolean selectionVisible;
    private Date now;
    private String spoilerAlert;

    public WriteupAdapter(AppCompatActivity context, ArrayList<Writeup> model) {
        super(context, model);

        now = new Date();

        Drawable emptyAvatar = context.getResources().getDrawable(R.drawable.empty_avatar);

        spoilerAlert = context.getResources().getString(R.string.spoiler_alert);

        imageDownloader = new ImageDownloader(context, emptyAvatar);
        imageGetterAsync = new ImageGetterAsync(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        displayVotingThumbs = prefs.getBoolean("display_voting_thumbs", true);

        selectionVisible = false;
    }

    public boolean getSelectionVisible() {
        return selectionVisible;
    }

    public void setSelectionVisible(boolean selectionVisible) {
        this.selectionVisible = selectionVisible;
    }

    public void setIsSelectedForAll(boolean isSelected) {
        if (model.size() > 0) {
            for (int i = model.size() - 1; i >= 0; i--) {
                Writeup wu = model.get(i);
                wu.IsSelected = isSelected;
            }
        }
    }

    public ImageGetterAsync getImageGetterAsync() {
        return imageGetterAsync;
    }

    public int getLastUnreadIndex() {
        int lastIndex = 0;

        if (model.size() > 0) {
            for (int i = model.size() - 1; i >= 0; i--) {
                Writeup wu = model.get(i);
                if (wu.Unread) {
                    lastIndex = i;
                    break;
                }
            }
        }

        return lastIndex;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        Writeup item = (Writeup) getItem(position);

        if (item.Type == Writeup.TYPE_DEFAULT || item.Type == Writeup.TYPE_LAST) {
            return 0;
        }

        if (item.Type == Writeup.TYPE_POLL) {
            return 1;
        }

        return 0; // co teraz ?
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        Writeup wu = (Writeup) getItem(position);

        if (row == null) {
            if (wu.Type == Writeup.TYPE_DEFAULT || wu.Type == Writeup.TYPE_LAST) {
                row = context.getLayoutInflater().inflate(R.layout.writeup_row, parent, false);
            } else if (wu.Type == Writeup.TYPE_POLL) {
                row = context.getLayoutInflater().inflate(R.layout.writeup_poll_row, parent, false);
            } else {
                // co teraz ?
                return null;
            }

            holder = new ViewHolder();
            holder.Thumbnail = (ImageView) row.findViewById(R.id.writeup_row_thumbnail);
            holder.Nick = (TextView) row.findViewById(R.id.writeup_row_nick);

            holder.Content = (TextView) row.findViewById(R.id.writeup_row_text);
            holder.Content.setLinkTextColor(appearance.getLinkColor());
            holder.Content.setMovementMethod(LinkMovementMethod.getInstance());
            holder.Content.setFocusable(false);

            holder.Time = (TextView) row.findViewById(R.id.writeup_row_time);
            holder.Rating = (TextView) row.findViewById(R.id.writeup_row_rating);
            holder.Location = (TextView) row.findViewById(R.id.writeup_row_location);

            holder.VoteUp = (ImageButton) row.findViewById(R.id.writeup_row_voteup);
            holder.VoteUp.setFocusable(false);
            holder.VoteUp.setFocusableInTouchMode(false);

            holder.VoteDown = (ImageButton) row.findViewById(R.id.writeup_row_votedown);
            holder.VoteDown.setFocusable(false);
            holder.VoteDown.setFocusableInTouchMode(false);

            holder.UnreadMarker = (ImageView) row.findViewById(R.id.writeup_row_unread_mark);

            holder.Selected = (CheckBox) row.findViewById(R.id.writeup_row_selected);

            if (!displayVotingThumbs) {
                holder.Rating.setVisibility(View.GONE);
                holder.VoteUp.setVisibility(View.GONE);
                holder.VoteDown.setVisibility(View.GONE);
            }

            if (wu.Type == Writeup.TYPE_POLL) {
                holder.AnswersContainer = (TableLayout) row.findViewById(R.id.writeup_poll_answers_container);
            }

            appearance.setFontSize(holder.Nick, holder.Content);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        if (appearance.isEntryUnreadColorStandard()) {
            if (wu.Unread) {
                holder.UnreadMarker.setVisibility(View.VISIBLE);
            } else {
                holder.UnreadMarker.setVisibility(View.INVISIBLE);
            }
        } else {
            holder.UnreadMarker.setVisibility(View.INVISIBLE);

            if (wu.Unread) {
                holder.Nick.setTextColor(appearance.getEntryUnreadColor());
                holder.Time.setTextColor(appearance.getEntryUnreadColor());
            } else {
                holder.Nick.setTextColor(appearance.getUseDarkTheme() ? Color.WHITE : Color.BLACK);
                holder.Time.setTextColor(appearance.getUseDarkTheme() ? Color.WHITE : Color.BLACK);
            }
        }

        imageDownloader.download(BasePoco.nickToUrl(wu.Nick, context), holder.Thumbnail);

        holder.Nick.setText(wu.Nick);

        holder.Content.setTag(position);

        if (wu.Type == Writeup.TYPE_DEFAULT || wu.Type == Writeup.TYPE_LAST) {
            if (wu.spoilerPresent()) {
                holder.Content.setText(CustomHtml.fromHtml(String.format("<font color='#666666'>%s</font>", spoilerAlert)));
            } else {
                holder.Content.setText(CustomHtml.fromHtml(wu.Content, imageGetterAsync.spawn(position, wu.Content, holder.Content)));
            }
        } else if (wu.Type == Writeup.TYPE_POLL) {
            final Poll poll = (Poll) wu;

            String content = String.format("<b>%s</b><br/>", poll.Question) + String.format("%s", poll.Instructions);
            holder.Content.setText(CustomHtml.fromHtml(content, imageGetterAsync.spawn(position, content, holder.Content)));

            holder.AnswersContainer.removeAllViewsInLayout();

            for (int answerIndex = 0; answerIndex < poll.Answers.size(); answerIndex++) {
                final PollAnswer answer = poll.Answers.get(answerIndex);

                TableRow tableRow = (TableRow) context.getLayoutInflater().inflate(R.layout.writeup_poll_row_answer, holder.AnswersContainer, false);

                Button answerButton = (Button) tableRow.findViewById(R.id.poll_answer_button);
                answerButton.setText(answer.Key);

                if (poll.UserDidVote && !answer.IsMyVote) {
                    answerButton.setVisibility(View.INVISIBLE);
                } else if (poll.UserDidVote && answer.IsMyVote) {
                    answerButton.setEnabled(false);
                } else if (!poll.UserDidVote) {
                    if (context instanceof IPollVotingHandler) {
                        answerButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ((IPollVotingHandler) context).onVote(position, answer.Key);
                            }
                        });
                    }
                }

                TextView answerTextView = (TextView) tableRow.findViewById(R.id.poll_answer_text);

                if (poll.UserDidVote) {
                    final double percentage = (((double) answer.RespondentsCount / (double) poll.TotalVotes) * 100.0);

                    final ProgressBar answerPercentage = (ProgressBar) tableRow.findViewById(R.id.poll_answer_progressbar);
                    answerPercentage.post(new Runnable() {
                        @Override
                        public void run() {
                            answerPercentage.setProgress((int) percentage);
                        }
                    });

                    answerTextView.setText(String.format("%s %.1f%% (%d)", answer.Answer, percentage, answer.RespondentsCount));
                } else {
                    answerTextView.setText(String.format("%s", answer.Answer));
                }

                holder.AnswersContainer.addView(tableRow, new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            }
        }

        holder.Time.setText(BasePoco.timeToString(context, wu.Time));

        if (wu.Location != null && wu.Location.valid()) {
            holder.Location.setVisibility(View.VISIBLE);
            holder.Location.setText(wu.Location.toString(now));
        } else {
            holder.Location.setVisibility(View.INVISIBLE);
        }

        if (displayVotingThumbs) {
            if (wu.Rating != 0) {
                holder.Rating.setText(CustomHtml.fromHtml(String.format("<font color='%s'>%d</font>", wu.Rating < 0 ? appearance.getVoteNegativeColor() : appearance.getVotePositiveColor(), wu.Rating)));
            } else {
                holder.Rating.setText("");
            }

            if (wu.IsMine) {
                holder.VoteUp.setVisibility(View.INVISIBLE);
                holder.VoteDown.setVisibility(View.INVISIBLE);
            } else {
                holder.VoteUp.setVisibility(View.VISIBLE);
                holder.VoteDown.setVisibility(View.VISIBLE);
            }
        }

        if (context instanceof IVotingHandler) {
            holder.VoteUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((IVotingHandler) context).onVote(position, VotingType.POSITIVE);
                }
            });

            holder.VoteDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((IVotingHandler) context).onVote(position, VotingType.NEGATIVE);
                }
            });
        }

        if (selectionVisible && (wu.Type == Writeup.TYPE_DEFAULT || wu.Type == Writeup.TYPE_LAST)) {
            holder.Selected.setVisibility(View.VISIBLE);
        } else {
            holder.Selected.setVisibility(View.GONE);
        }

        if (context instanceof IWriteupSelectionHandler && (wu.Type == Writeup.TYPE_DEFAULT || wu.Type == Writeup.TYPE_LAST)) {
            final Writeup selection = wu;

            holder.Selected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ((IWriteupSelectionHandler) context).onSelectionChanged(selection, isChecked);
                }
            });
        }

        holder.Selected.setChecked(wu.IsSelected);

        holder.setId(wu.Id);

        return row;
    }

    /**
     * @author Juraj
     */
    static class ViewHolder implements InformedViewHolder {
        public ImageView Thumbnail;
        public TextView Nick;
        public TextView Content;
        public TextView Time;
        public TextView Rating;
        public TextView Location;
        public ImageButton VoteUp;
        public ImageButton VoteDown;
        public ImageView UnreadMarker;
        public CheckBox Selected;
        public TableLayout AnswersContainer;

        private long id;

        public void setId(long id) {
            this.id = id;
        }

        @Override
        public long getId() {
            return this.id;
        }
    }
}
