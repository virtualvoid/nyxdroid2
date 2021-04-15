package sk.virtualvoid.nyxdroid.v2;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

import sk.virtualvoid.core.CoreUtility;
import sk.virtualvoid.core.ITaskQuery;
import sk.virtualvoid.core.ResponsibleBaseAdapter;
import sk.virtualvoid.core.Task;
import sk.virtualvoid.core.TaskListener;
import sk.virtualvoid.core.TaskManager;
import sk.virtualvoid.core.Tuple;
import sk.virtualvoid.nyxdroid.library.Constants;
import sk.virtualvoid.nyxdroid.library.Constants.WriteupDirection;
import sk.virtualvoid.nyxdroid.v2.am.WriteupsActionMode;
import sk.virtualvoid.nyxdroid.v2.am.WriteupsReplyMoreActionMode;
import sk.virtualvoid.nyxdroid.v2.data.BookmarkCategory;
import sk.virtualvoid.nyxdroid.v2.data.NullResponse;
import sk.virtualvoid.nyxdroid.v2.data.SuccessResponse;
import sk.virtualvoid.nyxdroid.v2.data.Writeup;
import sk.virtualvoid.nyxdroid.v2.data.WriteupBookmarkResponse;
import sk.virtualvoid.nyxdroid.v2.data.WriteupResponse;
import sk.virtualvoid.nyxdroid.v2.data.adapters.WriteupAdapter;
import sk.virtualvoid.nyxdroid.v2.data.dac.BookmarkDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.dac.WriteupDataAccess;
import sk.virtualvoid.nyxdroid.v2.data.query.WriteupBookmarkQuery;
import sk.virtualvoid.nyxdroid.v2.data.query.WriteupQuery;
import sk.virtualvoid.nyxdroid.v2.internal.IVotingHandler;
import sk.virtualvoid.nyxdroid.v2.internal.IWriteupSelectionHandler;
import sk.virtualvoid.nyxdroid.v2.internal.NavigationHandler;
import sk.virtualvoid.nyxdroid.v2.internal.NavigationType;
import sk.virtualvoid.nyxdroid.v2.internal.VotingResponse;
import sk.virtualvoid.nyxdroid.v2.internal.VotingType;
import sk.virtualvoid.nyxdroid.v2.internal.WriteupBookmarkQueryType;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

/**
 * @author Juraj
 */
public class WriteupsActivity extends BaseActivity implements IVotingHandler, IWriteupSelectionHandler, BaseFragment.Callbacks, TabListener {
    private long id;
    private Long requestedLastWuId;
    private String name;
    private boolean booked;
    private boolean canWrite;
    private boolean canDelete;

    private WriteupAdapter adapter;

    private static final int TAB_WRITEUPS = 0x01;
    private static final int TAB_HOME = 0x02;

    private Fragment currentFragment;
    private WriteupsFragment writeupsFragment;
    private WriteupsHomeFragment homeFragment;

    private boolean useBackPressWriteupReturn = false;
    private boolean refreshAfterWriteupSend = true;
    private ArrayDeque<Integer> previousPositions = new ArrayDeque<Integer>();
    private Task<WriteupQuery, WriteupResponse> tempDataTask = null;
    private Task<WriteupQuery, VotingResponse> tempRatingTask = null;
    private WriteupTaskListener writeupTaskListener = new WriteupTaskListener();
    private RatingTaskListener ratingTaskListener = new RatingTaskListener();
    private NoopTaskListener noopTaskListener = new NoopTaskListener();
    private WriteupResponsesTaskListener responsesTaskListener = new WriteupResponsesTaskListener();
    private BookOrUnbookTaskListener bookOrUnbookTaskListener = new BookOrUnbookTaskListener();
    private BookmarkCategoriesTaskListener bookmarkCategoriesTaskListener = new BookmarkCategoriesTaskListener();

    private SearchView searchView;
    private WriteupsActionMode amWriteups;
    private ActionMode amhWriteups;

    private WriteupsReplyMoreActionMode amReplyToMoreWriteupsCallback;
    private ActionMode amhReplyToMoreWriteups;
    private HashMap<Long, Writeup> mapReplyToMoreWriteups;

    @Override
    protected int getContentViewId() {
        return R.layout.empty_view;
    }

    @Override
    public ResponsibleBaseAdapter getResponsibleBaseAdapter() {
        return adapter;
    }

    @Override
    public String getTaskKey() {
        return String.format("%s%d", super.getTaskKey(), id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        id = extras.getLong(Constants.KEY_ID);
        name = extras.getString(Constants.KEY_TITLE);
        requestedLastWuId = extras.containsKey(Constants.KEY_WU_ID) ? extras.getLong(Constants.KEY_WU_ID) : null;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        useBackPressWriteupReturn = prefs.getBoolean("backpress_returns_writeup", false);
        refreshAfterWriteupSend = prefs.getBoolean("refresh_after_writeup_send", true);

        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        // nebol ten chuj uz vytvoreny ?
        homeFragment = (WriteupsHomeFragment) fm.findFragmentByTag(WriteupsHomeFragment.TAG);
        if (homeFragment == null) {
            homeFragment = new WriteupsHomeFragment();
        }
        if (!homeFragment.isAdded()) {
            ft.add(R.id.empty_view_ll, homeFragment, WriteupsHomeFragment.TAG);
            Log.w(Constants.TAG, "wu home add");
        }
        ft.hide(homeFragment);

        // nebol ten chuj uz vytvoreny ?
        currentFragment = writeupsFragment = (WriteupsFragment) fm.findFragmentByTag(WriteupsFragment.TAG);
        if (currentFragment == null) {
            currentFragment = writeupsFragment = new WriteupsFragment();
        }
        if (!writeupsFragment.isAdded()) {
            ft.add(R.id.empty_view_ll, writeupsFragment, WriteupsFragment.TAG);
            Log.w(Constants.TAG, "wu list add");
        }
        ft.show(writeupsFragment);

        ft.commit();

        actionBar.addTab(actionBar.newTab().setTag(TAB_WRITEUPS).setText(R.string.tab_title_discussion).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setTag(TAB_HOME).setText(R.string.tab_title_headerhome).setTabListener(this));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong("id", id);

        if (requestedLastWuId != null) {
            outState.putLong("requestedLastWuId", requestedLastWuId);
        }

        outState.putString("name", name);
        outState.putBoolean("booked", booked);
        outState.putBoolean("canWrite", canWrite);
        outState.putBoolean("canDelete", canDelete);
        outState.putBoolean("refreshAfterWriteupSend", refreshAfterWriteupSend);
        outState.putBoolean("useBackPressWriteupReturn", useBackPressWriteupReturn);

        Log.w(Constants.TAG, "WU SAVE INSTANCE");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        id = savedInstanceState.getLong("id");
        requestedLastWuId = savedInstanceState.containsKey("requestedLastWuId") ? savedInstanceState.getLong("requestedLastWuId") : null;
        name = savedInstanceState.getString("name");
        booked = savedInstanceState.getBoolean("booked");
        canWrite = savedInstanceState.getBoolean("canWrite");
        canDelete = savedInstanceState.getBoolean("canDelete");
        refreshAfterWriteupSend = savedInstanceState.getBoolean("refreshAfterWriteupSend");
        useBackPressWriteupReturn = savedInstanceState.getBoolean("useBackPressWriteupReturn");

        Log.w(Constants.TAG, "WU RESTORE INSTANCE");
    }

    @Override
    public void onListViewCreated(ListView listView, Bundle savedInstanceState) {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View row, int position, long id) {
                Writeup wu = (Writeup) parent.getItemAtPosition(position);
                composeOne(wu);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View row, int position, long id) {
                final Writeup wu = (Writeup) parent.getItemAtPosition(position);

                amWriteups = new WriteupsActionMode(WriteupsActivity.this, new WriteupsActionMode.Listener() {
                    @Override
                    public void onReply() {
                        amhWriteups.finish();

                        composeOne(wu);
                    }

                    @Override
                    public void onSendMail() {
                        amhWriteups.finish();

                        Intent intent = new Intent(WriteupsActivity.this, MailComposeActivity.class);
                        intent.putExtra(Constants.KEY_NICK, wu.Nick);
                        startActivity(intent);
                        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                    }

                    @Override
                    public void onViewReplies() {
                        amhWriteups.finish();

                        WriteupQuery query = new WriteupQuery();

                        query.Id = WriteupsActivity.this.id;
                        query.LastId = wu.Id;
                        query.Direction = WriteupDirection.WRITEUP_DIRECTION_NEWER;
                        //query.FilterContents = Long.toString(wu.Id);

                        TaskManager.killIfNeeded(tempDataTask);

                        tempDataTask = WriteupDataAccess.getWriteups(WriteupsActivity.this, responsesTaskListener);
                        TaskManager.startTask(tempDataTask, query);
                    }

                    @Override
                    public void onViewRating() {
                        amhWriteups.finish();

                        WriteupRatingsDialog dialog = new WriteupRatingsDialog(WriteupsActivity.this, WriteupsActivity.this.id, wu.Id);
                        dialog.show();
                    }

                    @Override
                    public void onViewGallery() {
                        amhWriteups.finish();

                        gallery(wu.Id, null);
                    }

                    @Override
                    public void onReminder() {
                        amhWriteups.finish();

                        WriteupQuery query = new WriteupQuery();
                        query.Id = WriteupsActivity.this.id;
                        query.TempId = wu.Id;

                        Task<WriteupQuery, NullResponse> task = WriteupDataAccess.reminder(WriteupsActivity.this, noopTaskListener);
                        TaskManager.startTask(task, query);
                    }

                    @Override
                    public void onDelete() {
                        amhWriteups.finish();

                        WriteupQuery query = new WriteupQuery();
                        query.Id = WriteupsActivity.this.id;
                        query.TempId = wu.Id;
                        query.IsDeleting = true;

                        Task<WriteupQuery, NullResponse> task = WriteupDataAccess.delete(WriteupsActivity.this, noopTaskListener);
                        TaskManager.startTask(task, query);
                    }

                    @Override
                    public void onCopy() {
                        amhWriteups.finish();

                        Item item = new Item(wu.Content.replaceAll("\\<.*?>", ""));
                        ClipData data = new ClipData(new ClipDescription("WU Text Copied", new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}), item);
                        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboardManager.setPrimaryClip(data);
                    }

                    @Override
                    public void onCopyLink() {
                        // www.nyx.cz/discussion/21293/id/39531584
                        amhWriteups.finish();

                        Item item = new Item(String.format("%s/discussion/%d/id/%d", Constants.INDEX, WriteupsActivity.this.id, wu.Id));
                        ClipData data = new ClipData(new ClipDescription("WU Link Copied", new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}), item);
                        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboardManager.setPrimaryClip(data);
                    }
                });

                amhWriteups = startActionMode(amWriteups);

                return true;
            }
        });

        setupListViewInstance(listView);
        setListView(listView);

        getPullToRefreshAttacher().setRefreshableView(listView, new PullToRefreshAttacher.OnRefreshListener() {
            @Override
            public void onRefreshStarted(View view) {
                load(null, null, false);
            }
        });

        load(null, requestedLastWuId,/* requestedLastId != null */false);
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        if (currentFragment != null) {
            ft.hide(currentFragment);
        }

        if (TAB_WRITEUPS == (Integer) tab.getTag()) {
            currentFragment = writeupsFragment;
        }

        if (TAB_HOME == (Integer) tab.getTag()) {
            homeFragment.load(id);
            currentFragment = homeFragment;
        }

        ft.show(currentFragment);
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onBackPressed() {
        if (useBackPressWriteupReturn) {
            // mame nejake ulozene pozicie v ramci diskusie v pohybe ?
            // ak ano previousPosition nieje null a nastavime listview na
            // priblizne
            // to miesto
            Integer previousPosition = previousPositions.poll();
            if (previousPosition == null) {
                super.onBackPressed();
            } else {
                getListView().setSelection(previousPosition);
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.writeups_menu, menu);

        // vyhladavacie menu
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                if (searchView != null) {
                    searchView.requestFocus();
                }
                Log.d(Constants.TAG, "SearchView on SearchActivity expanded.");
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                Log.d(Constants.TAG, "SearchView on SearchActivity closed.");
                return true;
            }
        });

        searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // book alebo unbook menu, updatne sa po nacitani WU meta informacii
        MenuItem unbookMenuItem = menu.findItem(R.id.book_or_unbook);
        if (booked) {
            unbookMenuItem.setTitle(R.string.book_or_unbook_title_unbook);
        } else {
            unbookMenuItem.setTitle(R.string.book_or_unbook_title_book);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.compose:
                return composeOne(null);
            case R.id.refresh:
                return load(null, null, false);
            case R.id.gallery:
                return galleryGrid();
            case R.id.replytomorewriteups:
                return replyToMoreWriteups();
            case R.id.book_or_unbook:
                return bookOrUnbook();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST && resultCode == Constants.REQUEST_RESPONSE_OK) {
            // only refresh if it's in settings [always is by default design]
            if (refreshAfterWriteupSend) {
                load(null, null, false);
            }
        }
        if (requestCode == Constants.REQUEST && resultCode == Constants.REQUEST_RESPONSE_VOTING) {
            long wuId = data.getLongExtra(Constants.KEY_ID, 0);
            int position = adapter.getItemPosition(wuId);
            VotingType votingType = (VotingType) data.getSerializableExtra(Constants.KEY_VOTING_RESULT);
            onVote(position, votingType);
        }
        if (requestCode == Constants.REQUEST_GALLERY && resultCode == Constants.REQUEST_RESPONSE_OK) {
            galleryResultHandler(data.getLongExtra(Constants.KEY_WU_ID, 0));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onListViewDataRequested() {
        Writeup last = null;
        if (adapter != null && (last = adapter.getLastItem()) != null) {
            load(null, last.Id, false);
            Log.d(Constants.TAG, "Writeups last id: " + last.Id);
        }
    }

    @Override
    public boolean onNavigationRequested(NavigationType navigationType, String url, Long discussionId, Long writeupId) {
        // completny override z base activity !
        if (navigationType == NavigationType.NONE || (discussionId == null && writeupId == null && url == null)) {
            return false;
        }

        boolean result = true;

        if (navigationType == NavigationType.TOPIC) {
            int position = -1;

            if (discussionId == id && writeupId != null) {
                // ulozime aktualnu poziciu pre navrat k predoslemu prispevku v
                // ramci diskusie
                previousPositions.push(getListView().getFirstVisiblePosition());

                // ak sme v jednej a tej istej diskusii pohladame poziciu wu_id
                if (-1 == (position = adapter.getItemPosition(writeupId))) {
                    // nacitame nove prispevky lebo pozicia sa nenasla, posledny
                    // parameter hovori otom aby sa riadok focusol
                    load(null, writeupId, true);
                } else {
                    // nasli sme poziciu
                    getListView().setSelection(position);
                }
            }

            if (discussionId != id) {
                // STARE: ak niesme v rovnakej diskusii nacitame ju od daneho
                // prispevku
                // load(discussionId, writeupId != null ? writeupId + 1 : null,
                // false);
                // NOVE: ak niesme v rovnakej diskusii spustime jej novu
                // aktivitu
                // (kvoli navratu k historii)
                NavigationHandler.startNavigateTopic(this, WriteupsActivity.class, discussionId, (writeupId != null ? writeupId + 1 : null));
            }
        }

        if (navigationType == NavigationType.EVENT) {
            //NavigationHandler.startNavigateEvent(this, EventActivity.class, discussionId, null);
            result = false;
        }

        if (navigationType == NavigationType.IMAGE) {
            try {
                String decodedUrl = URLDecoder.decode(url, Constants.DEFAULT_CHARSET.displayName());
                gallery(writeupId, decodedUrl);
            } catch (UnsupportedEncodingException e) {
                result = false;
            }
        }

        if (navigationType == NavigationType.MARKET) {
            //NavigationHandler.startNavigateMarket(this, AdvertActivity.class, discussionId);
            result = false;
        }

        return result;
    }

    @Override
    public void onVote(int position, VotingType votingType) {
        Writeup wu = (Writeup) adapter.getItem(position);

        WriteupQuery query = new WriteupQuery();
        query.Id = id;
        query.TempId = wu.Id;
        query.VotingType = votingType;
        query.VotingPosition = position;

        vote(query);
    }

    private boolean load(final Long discussionId, final Long lastId, final boolean lastSelected) {
        /**
         *
         * @param discussionId
         *            cielove id diskusie, null ak je to v ramci jednej
         * @param lastId
         *            pouziva sa pri docitavani prispevkov, alebo nastavenie
         *            focusu na riadok (lastSelected=true)
         * @param lastSelected
         *            nastavit focus na riadok daneho wu id v lastid ?
         * @return
         */
        WriteupQuery query = new WriteupQuery();

        query.NavigatingOutside = discussionId != null;
        query.Id = discussionId == null ? id : discussionId;
        query.LastId = lastId;
        query.LastSelected = lastSelected;
        query.Direction = lastId == null ? WriteupDirection.WRITEUP_DIRECTION_NEWEST : WriteupDirection.WRITEUP_DIRECTION_OLDER;

        TaskManager.killIfNeeded(tempDataTask);

        tempDataTask = WriteupDataAccess.getWriteups(this, writeupTaskListener);
        TaskManager.startTask(tempDataTask, query);

        return true;
    }

    private boolean loadFilter(String filterUser, String filterText) {
        WriteupQuery query = new WriteupQuery();

        query.Id = id;
        query.Direction = WriteupDirection.WRITEUP_DIRECTION_NEWEST;
        query.FilterUser = filterUser;
        query.FilterContents = filterText;

        TaskManager.killIfNeeded(tempDataTask);

        tempDataTask = WriteupDataAccess.getWriteups(WriteupsActivity.this, writeupTaskListener);
        TaskManager.startTask(tempDataTask, query);
        return true;
    }

    private boolean composeOne(Writeup wu) {
        ArrayList<Writeup> wuList = new ArrayList<Writeup>();

        if (wu != null) {
            wuList.add(wu);
        }

        return composeMore(wuList);
    }

    private boolean composeMore(ArrayList<Writeup> wuList) {
        // TODO: nyx to zatial nema v api vyriesene
//		if (!canWrite) {
//			Toast.makeText(WriteupsActivity.this, R.string.you_dont_have_write_rights, Toast.LENGTH_SHORT).show();
//			return true;
//		}

        final Intent intent = new Intent(WriteupsActivity.this, WriteupComposeActivity.class);

        intent.putExtra(Constants.REQUEST_WRITEUP_DISCUSSION_ID, id);
        intent.putExtra(Constants.REQUEST_WRITEUP_DISCUSSION_NAME, name);

        if (wuList != null && wuList.size() > 0) {
            intent.putExtra(Constants.REQUEST_WRITEUP, wuList);
        }

        startActivityForResult(intent, Constants.REQUEST);

        return true;
    }

    private void search(String text) {
        Tuple<String, String> tuple = CoreUtility.splitSearch(text);
        if (tuple == null) {
            Log.w(Constants.TAG, "Empty search term.");
            return;
        }

        loadFilter(tuple.First, tuple.Second);
    }

    private boolean galleryGrid() {
        if (adapter == null || adapter.getCount() == 0) {
            Log.w(Constants.TAG, "Unable to make galleryGrid. Empty adapter.");
            return false;
        }

        Intent intent = new Intent(WriteupsActivity.this, GalleryGridActivity.class);
        intent.putExtra(Constants.KEY_ID, id);

        startActivity(intent);
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);

        return true;
    }

    private boolean gallery(Long startWriteupId, String startUrl) {
        if (adapter == null || adapter.getCount() == 0) {
            Log.w(Constants.TAG, "Unable to make gallery. Empty adapter.");
            return false;
        }

        ArrayList<Bundle> infoBundleList = new ArrayList<Bundle>();

        ArrayList<Writeup> writeupList = adapter.getItems();
        for (int i = 0; i < writeupList.size(); i++) {
            Writeup wu = writeupList.get(i);
            ArrayList<Bundle> infoWu = wu.allImages();
            if (infoWu.size() > 0) {
                infoBundleList.addAll(infoWu);
            }
        }

        if (infoBundleList.size() == 0) {
            Toast.makeText(this, R.string.discussion_doesnt_contain_images, Toast.LENGTH_SHORT).show();
            return false;
        }

        Bundle[] infoArray = infoBundleList.toArray(new Bundle[infoBundleList.size()]);

        Intent intent = new Intent(WriteupsActivity.this, GalleryActivity.class);

        if (startWriteupId != null) {
            intent.putExtra(Constants.KEY_WU_ID, startWriteupId);
        }

        if (startUrl != null) {
            intent.putExtra(Constants.KEY_URL, startUrl);
        }

        intent.putExtra(Constants.KEY_BUNDLE_ARRAY, infoArray);
        startActivityForResult(intent, Constants.REQUEST_GALLERY);
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);

        return true;
    }

    private void galleryResultHandler(long wuId) {
        if (wuId == 0 || adapter == null) {
            return;
        }

        int position = adapter.getItemPosition(wuId);
        getListView().setSelection(position);
    }

    private void vote(WriteupQuery query) {
        TaskManager.killIfNeeded(tempRatingTask);

        tempRatingTask = WriteupDataAccess.giveRating(WriteupsActivity.this, ratingTaskListener);
        TaskManager.startTask(tempRatingTask, query);
    }

    private boolean replyToMoreWriteups() {
        if (mapReplyToMoreWriteups == null) {
            mapReplyToMoreWriteups = new HashMap<Long, Writeup>();
        }
        mapReplyToMoreWriteups.clear();

        amhReplyToMoreWriteups = startActionMode(amReplyToMoreWriteupsCallback = new WriteupsReplyMoreActionMode(this, new WriteupsReplyMoreActionMode.Listener() {
            @Override
            public void onReply() {
                amhReplyToMoreWriteups.finish();

                ArrayList<Writeup> wuList = new ArrayList<Writeup>(mapReplyToMoreWriteups.values());
                composeMore(wuList);
            }

            @Override
            public void onDestroy() {
                adapter.setSelectionVisible(false);
                adapter.setIsSelectedForAll(false);
                adapter.notifyDataSetChanged();
            }
        }));

        adapter.setSelectionVisible(true);
        adapter.notifyDataSetChanged();

        return true;
    }

    @Override
    public void onSelectionChanged(Writeup writeup, boolean isSelected) {
        if (isSelected && !mapReplyToMoreWriteups.containsKey(writeup.Id)) {
            mapReplyToMoreWriteups.put(writeup.Id, writeup);
        }

        if (!isSelected && mapReplyToMoreWriteups.containsKey(writeup.Id)) {
            mapReplyToMoreWriteups.remove(writeup.Id);
        }

        writeup.IsSelected = isSelected;

        adapter.notifyDataSetChanged();
    }

    private boolean bookOrUnbook() {
        if (booked) {
            // ak mame v bookmarkoch, tak ju ideme odtial dat prec
            bookOrUnbook(null);
        } else {
            // ak nemame zaradene v bookmarkoch tak najprv zistime kategoriu kam
            // ju chce dat
            Task<ITaskQuery, SuccessResponse<ArrayList<BookmarkCategory>>> task = BookmarkDataAccess.getBookmarkCategories(this, bookmarkCategoriesTaskListener);
            TaskManager.startTask(task, ITaskQuery.empty);
        }

        return true;
    }

    private boolean bookOrUnbook(BookmarkCategory category) {
        WriteupBookmarkQuery query = new WriteupBookmarkQuery();
        query.DiscussionId = id;
        query.CategoryId = category != null ? category.Id : null;
        query.QueryType = booked ? WriteupBookmarkQueryType.UNBOOK : WriteupBookmarkQueryType.BOOK;

        Task<WriteupBookmarkQuery, WriteupBookmarkResponse> task = WriteupDataAccess.bookOrUnbookWriteup(this, bookOrUnbookTaskListener);
        TaskManager.startTask(task, query);

        return true;
    }

    private class WriteupTaskListener extends TaskListener<WriteupResponse> {
        @Override
        public void done(WriteupResponse output) {
            WriteupQuery query = (WriteupQuery) getTag();
            WriteupsActivity context = (WriteupsActivity) getContext();

            if (adapter == null || query.NavigatingOutside || query.Direction == WriteupDirection.WRITEUP_DIRECTION_NEWEST) {
                adapter = new WriteupAdapter(context, output.Writeups);
                context.setListAdapter(adapter);
                context.getListView().setSelection(adapter.getLastUnreadIndex());
            } else {
                adapter.addItems(adapter.filter(output.Writeups));
                adapter.notifyDataSetChanged();
            }

            if (query.LastSelected && query.LastId != null) {
                int position = adapter.getItemPosition(query.LastId);
                getListView().setSelection(position);

                if (position == -1) {
                    previousPositions.pop();
                    Toast.makeText(context, "Uhm...", Toast.LENGTH_SHORT).show();
                }
            }

            id = output.Id;
            setTitle(name = output.Name);

            booked = output.Booked;
            canWrite = output.CanWrite;
            canDelete = output.CanDelete;

            // updatneme info v menu, kvoli book/unbook etc.
            invalidateOptionsMenu();

            getPullToRefreshAttacher().setRefreshComplete();
        }
    }

    private class RatingTaskListener extends TaskListener<VotingResponse> {
        @Override
        public void done(VotingResponse output) {
            WriteupsActivity context = (WriteupsActivity) getContext();

            final WriteupQuery query = (WriteupQuery) getTag();
            query.VotingConfirmed = true;

            switch (output.Result) {
                case RATING_CHANGED:
                case RATING_GIVEN:
                case RATING_REMOVED:
                    if (output.CurrentRating != null) {
                        Writeup wu = adapter.getItemById(query.TempId);
                        wu.Rating = output.CurrentRating;
                        adapter.notifyDataSetChanged();
                    }
                    break;

                case RATING_NEEDS_CONFIRMATION:
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                    dialogBuilder.setTitle(R.string.anonymous_vote_warning_title);
                    dialogBuilder.setMessage(R.string.anonymous_vote_warning);

                    dialogBuilder.setNegativeButton(R.string.negative_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    dialogBuilder.setPositiveButton(R.string.positive_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            vote(query);
                        }
                    });

                    AlertDialog dialog = dialogBuilder.create();
                    dialog.show();
                    break;
            }
        }
    }

    private class NoopTaskListener extends TaskListener<NullResponse> {
        @Override
        public void done(NullResponse output) {
            WriteupQuery query = (WriteupQuery) getTag();
            WriteupsActivity context = (WriteupsActivity) getContext();

            if (query.IsDeleting && output.Success) {
                Writeup wu = adapter.getItemById(query.TempId);
                adapter.removeItem(wu);

                context.notifyDatasetChanged();
            }
        }
    }

    private class BookmarkCategoriesTaskListener extends TaskListener<SuccessResponse<ArrayList<BookmarkCategory>>> {
        @Override
        public void done(SuccessResponse<ArrayList<BookmarkCategory>> response) {
            final ArrayList<BookmarkCategory> categories = response.getData();
            if (categories.size() == 0) {
                Toast.makeText(getContext(), R.string.add_discussion_to_category_no_categories, Toast.LENGTH_LONG).show();
                return;
            }

            String[] categoryTitles = new String[categories.size()];
            for (int i = 0; i < categories.size(); i++) {
                BookmarkCategory category = categories.get(i);
                categoryTitles[i] = category.Name;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

            builder.setTitle(R.string.add_discussion_to_category_title);
            builder.setItems(categoryTitles, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    BookmarkCategory selected = categories.get(which);
                    bookOrUnbook(selected);
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private class BookOrUnbookTaskListener extends TaskListener<WriteupBookmarkResponse> {
        @Override
        public void done(WriteupBookmarkResponse output) {
            // ako to dopadlo ? invertnut stav nestaci, pretoze unbook/book sa
            // nemusel podarit
            booked = output.Success && output.Booked;

            // updatneme info v menu, kvoli book/unbook etc.
            invalidateOptionsMenu();
        }
    }

    private class WriteupResponsesTaskListener extends TaskListener<WriteupResponse> {
        @Override
        public void done(WriteupResponse output) {
            WriteupQuery query = (WriteupQuery) getTag();
            WriteupsActivity context = (WriteupsActivity) getContext();

            ArrayList<Writeup> model = output.Writeups;

            if (model.size() == 0) {
                Toast.makeText(getContext(), R.string.no_responses_for_this_writeup, Toast.LENGTH_SHORT).show();
                return;
            }

            adapter = new WriteupAdapter(context, model);
            context.setListAdapter(adapter);
        }
    }

}
