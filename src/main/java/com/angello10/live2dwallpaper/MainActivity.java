package com.angello10.live2dwallpaper;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import java.util.*;
import java.io.*;
import android.widget.SeekBar.*;
import android.graphics.*;
import android.widget.AdapterView.*;
import android.content.*;


public class MainActivity extends Activity {
	ListView listView1, listView2, listView3;
	SeekBar seek1, seek2, seek3;
	ArrayAdapter moc, mtn, texture;
	TextView text;
	
	int select = 0;
	String[] list;
	String directory;
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		text = (TextView)findViewById(R.id.text1);
		
		class mySeekBarListener implements OnSeekBarChangeListener {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				text.setTextColor(Color.argb(255, seek1.getProgress(), seek2.getProgress(), seek3.getProgress()));
			}

			@Override
			public void onStopTrackingTouch(SeekBar p1) {}

			@Override
			public void onStartTrackingTouch(SeekBar p1) {} 
		}
		
		seek1 = (SeekBar)findViewById(R.id.seek1);
		seek1.setOnSeekBarChangeListener(new mySeekBarListener());
		seek2 = (SeekBar)findViewById(R.id.seek2);
		seek2.setOnSeekBarChangeListener(new mySeekBarListener());
		seek3 = (SeekBar)findViewById(R.id.seek3);
		seek3.setOnSeekBarChangeListener(new mySeekBarListener());
		
		listView1 = (ListView)findViewById(R.id.list1);

		List<String> l = new ArrayList<String>();
		moc = new ArrayAdapter(this, android.R.layout.simple_list_item_1, l);
		moc.add("...");
		listView1.setAdapter(moc);
		listView1.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick (AdapterView parent, View view, int position, long id) {
				select = 0;
				chooseFile(new File("/sdcard"));
			}
		});

		listView2 = (ListView)findViewById(R.id.list2);

		l = new ArrayList<String>();
		mtn = new ArrayAdapter(this, android.R.layout.simple_list_item_1, l);
		mtn.add("...");
		listView2.setAdapter(mtn);
		listView2.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick (AdapterView parent, View view, int position, long id) {
				select = 1;
				chooseFile(new File("/sdcard"));
			}
		});

		listView3 = (ListView)findViewById(R.id.list3);

		l = new ArrayList<String>();
		texture = new ArrayAdapter(this, android.R.layout.simple_list_item_1, l);
		texture.add("...");
		listView3.setAdapter(texture);
		
		listView3.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick (AdapterView parent, View view, int position, long id) {
				select = position + 2;
				chooseFile(new File("/sdcard"));
			}
		});
		listView3.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick (AdapterView parent, View view, int position, long id) {
				texture.remove(texture.getItem(position));
				return false;
			}
		});

		try{
			BufferedReader br = new BufferedReader(new FileReader("sdcard/live2d.txt"));
			String read;
			while((read = br.readLine()) != null) {
				read = read.replaceAll(" ", "");
				String[] data;
				if(read.indexOf(":") == -1) continue;
				data = read.split(":");
				switch(data[0]) {
					case "texture_paths":
						texture.clear();
						String[] t = data[1].indexOf(",") == -1 ? new String[]{data[1]} : data[1].split(",");
						for (String s : t) texture.add(s);
						break;
					case "model_path":
						moc.clear();
						moc.add(data[1]);
						break;
					case "motion_path":
						mtn.clear();
						mtn.add(data[1]);
						break;
					case "background_color":
						String[] color = data[1].split(",");
						seek1.setProgress((int)(256 * Float.parseFloat(color[0])));
						seek2.setProgress((int)(256 * Float.parseFloat(color[1])));
						seek3.setProgress((int)(256 * Float.parseFloat(color[2])));
				}
			}
		}catch(Exception error) {}
    }
	
    public void addList(View v) {
        texture.add("...");
    }
	
	public void save(View v) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("sdcard/live2d.txt"));
			
			int count = texture.getCount();
			if (count > 0) bw.write("texture_paths : ");
			for(int i = 0;i < count;i++) {
				bw.write(texture.getItem(i) + ((i + 1) == count ? "\n" : ", "));
			}
			
			bw.write("model_path : ");
			bw.write(moc.getItem(0) + "\n");
			
			bw.write("motion_path : ");
			bw.write(mtn.getItem(0) + "\n");
			
			bw.write("background_color : ");
			bw.write((float)seek1.getProgress() / 256 + ", " + (float)seek2.getProgress() / 256 + ", " + (float)seek3.getProgress() / 256);
			bw.close();
			
			Toast.makeText(this, "save", 1).show();
		}catch(Exception error) {Toast.makeText(this, error+"", 1).show();}
	}
	
	public void chooseFile(File dir) {
		if(dir.isFile()) {
			if (select == 0) {
				moc.remove(moc.getItem(0));
				moc.insert(dir.getAbsolutePath() ,0);
			}
			else if (select == 1) {
				mtn.remove(mtn.getItem(0));
				mtn.insert(dir.getAbsolutePath() ,0);
			}
			else {
				texture.remove(texture.getItem(select - 2));
				texture.insert(dir.getAbsolutePath() ,select - 2);
			}
		}
		else if (dir.isDirectory()) {try{
			directory = dir.getAbsolutePath();
			String[] flist = dir.list();
			Arrays.sort(flist);
			
			if (dir.getParentFile() != null) {
				list = new String[flist.length + 1];
				list[0] = "...";
				System.arraycopy(flist, 0, list, 1, flist.length);
			}
			else list = flist;
			
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setItems(list, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int position) {
					chooseFile(list[position].equals("...") ? new File(directory).getParentFile() : new File(directory, list[position]));
				}
			});
			alert.show();}catch(Exception e) {Toast.makeText(this, e+"",1).show();}
		}
	}
}
