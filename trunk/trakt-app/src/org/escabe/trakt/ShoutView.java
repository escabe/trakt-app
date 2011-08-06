package org.escabe.trakt;

import org.json.JSONArray;
import org.json.JSONObject;

import com.commonsware.cwac.thumbnail.ThumbnailAdapter;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SlidingDrawer.OnDrawerOpenListener;

public class ShoutView extends SlidingDrawer implements OnDrawerOpenListener, ActivityWithUpdate {
	private ListView list;
	private ProgressBar progress;
	private EditText editShout;
	private Button buttonShout;
	private JSONArray data;
	private TraktAPI traktapi;
	private Context context;
	private String viewurl;
	private View me;
	
	private ThumbnailAdapter thumbs=null;
	private static final int[] IMAGE_IDS={R.id.imageAvatar};

	public ShoutView(Context context, AttributeSet attrs) {
		super(context, attrs);
		me = this;
		// Add standard content
        LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View item = vi.inflate(R.layout.trakt_shout_view, null);
		this.addView(item);
	
		// Set listeners
		this.setOnDrawerOpenListener(this);
		
		// Configure locals
		this.context = context;
		traktapi = new TraktAPI(context);
		progress = (ProgressBar) findViewById(R.id.progressShout);
		list = (ListView) findViewById(R.id.listShouts);
		viewurl = null;

		buttonShout = (Button) findViewById(R.id.buttonShout);
		buttonShout.setOnClickListener(OnButtonClick);
		
		editShout = (EditText) findViewById(R.id.editShout);
		editShout.setOnFocusChangeListener(EditFocusChanged);

		list.requestFocus();
		
		Activity act = (Activity)context;		
		thumbs = new ThumbnailAdapter(act, new ShoutAdapter(), ((Application)act.getApplication()).getThumbsCache(),IMAGE_IDS);
		list.setAdapter(thumbs);
	}

	private OnClickListener OnButtonClick = new OnClickListener() {
		public void onClick(View view) {
			String shout = editShout.getText().toString();
			if (shout.length()<1) {
				Toast.makeText(context, "Please enter a shout.",Toast.LENGTH_SHORT).show();
				return;
			}
			if (viewurl.contains("movie/")) {
				String p[] = viewurl.split("/");
				String id = p[p.length-1];
				traktapi.Shout(me, "movie",shout,id);
			} else if (viewurl.contains("episode/")) {
				String p[] = viewurl.split("/");
				String id = p[p.length-3];
				Integer season = Integer.parseInt(p[p.length-2]);
				Integer episode = Integer.parseInt(p[p.length-1]);
				traktapi.Shout(me, "episode",shout,id,season,episode);
			} else if (viewurl.contains("show/")) {
				String p[] = viewurl.split("/");
				String id = p[p.length-1];
				traktapi.Shout(me, "show",shout,id);
			}
		}
	};
	
	private OnFocusChangeListener EditFocusChanged = new OnFocusChangeListener() {
		public void onFocusChange(View v, boolean hasFocus) {
			EditText edit = (EditText)v;
			if (hasFocus) {
				edit.setLines(3);
				buttonShout.setVisibility(VISIBLE);
			} else {
				edit.setLines(1);
				buttonShout.setVisibility(GONE);
			}
			
		}

	};
	
	public void onDrawerOpened() {
		if (viewurl==null) {
			Toast.makeText(context, "Please wait for details to load.",Toast.LENGTH_SHORT).show();
			this.animateClose();
			return;
		}
		DataGrabber dg = new DataGrabber();
		dg.execute(viewurl);
	}
	
	public void setViewurl(String viewurl) {
		this.viewurl = viewurl;
	}

	public String getViewurl() {
		return viewurl;
	}

	private class DataGrabber extends AsyncTask<String,Void,Boolean> {
		public DataGrabber() {
		}

		@Override
		protected void onPreExecute() {
			progress.setVisibility(VISIBLE);
			thumbs.notifyDataSetInvalidated();
		}
    	
		@Override
		protected Boolean doInBackground(String... params) {
			data = traktapi.getDataArrayFromJSON(params[0], true); 
			if (data==null) {
				return false;
			}
			return true;
		}
		@Override
	    protected void onPostExecute(Boolean result) {
			if (!result) {
				progress.setVisibility(GONE);
				Toast.makeText(context, "Failed loading shouts.",Toast.LENGTH_SHORT).show();
				return;
			}
			progress.setVisibility(GONE);
			thumbs.notifyDataSetChanged();
		}
    }
	
	class ShoutAdapter extends BaseAdapter {
		public int getCount() {
			if (data==null)
				return 0;
			return data.length();
		}

		public Object getItem(int arg0) {
			if (data==null)
				return null;
			return data.optJSONObject(arg0);
		}

		public long getItemId(int arg0) {
			return arg0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
            if (row == null) { //Create new row when needed
                LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = vi.inflate(R.layout.trakt_shout_row, null);
            }
            JSONObject info = (JSONObject) getItem(position);
            if (info != null) {
            	
            	ImageView ava = (ImageView)row.findViewById(R.id.imageAvatar);
            	ava.setImageResource(R.drawable.ic_item_avatar);
            	ava.setTag(traktapi.ResizeAvatar(info.optJSONObject("user").optString("avatar"),3));
            	
            	TextView shouter = (TextView)row.findViewById(R.id.textShouter);
            	shouter.setText(info.optJSONObject("user").optString("username"));

            	TextView shout = (TextView)row.findViewById(R.id.textShout);
            	shout.setText(info.optString("shout"));

            }
			return row;
		}
	}

	public void DoUpdate() {
		onDrawerOpened();
	}
	
}
