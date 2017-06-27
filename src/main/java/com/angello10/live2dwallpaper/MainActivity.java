package com.angello10.live2dwallpaper;

import android.app.*;
import android.databinding.DataBindingUtil;
import android.os.*;
import android.view.*;
import android.widget.*;

import java.util.*;
import java.io.*;
import android.widget.SeekBar.*;
import android.graphics.*;
import android.widget.AdapterView.*;
import android.content.*;

import com.angello10.live2dwallpaper.databinding.ActivityMainBinding;

//TODO:StoragePermission
public class MainActivity extends Activity {
	ListView listView3;
	ArrayAdapter moc, mtn, texture;

	int select = 0;
	String[] list;
	String directory;

	ActivityMainBinding binding;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        binding = DataBindingUtil.setContentView( this , R.layout.activity_main );

		class mySeekBarListener implements OnSeekBarChangeListener {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				binding.text1.setTextColor(Color.rgb( binding.redSeek.getProgress(), binding.greenSeek.getProgress(), binding.blueSeek.getProgress()));
			}

			@Override
			public void onStopTrackingTouch(SeekBar p1) {}

			@Override
			public void onStartTrackingTouch(SeekBar p1) {} 
		}
		
		binding.redSeek.setOnSeekBarChangeListener( new mySeekBarListener());
        binding.greenSeek.setOnSeekBarChangeListener( new mySeekBarListener());
        binding.blueSeek.setOnSeekBarChangeListener( new mySeekBarListener());

        binding.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });







		List<String> l = new ArrayList<>();
		moc = new ArrayAdapter(this, android.R.layout.simple_list_item_1, l);
		moc.add("...");
		binding.mocList.setAdapter(moc);
        binding.mocList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick (AdapterView parent, View view, int position, long id) {
				select = 0;
				chooseFile(new File("/sdcard"));
			}
		});

		l = new ArrayList<>();
		mtn = new ArrayAdapter(this, android.R.layout.simple_list_item_1, l);
		mtn.add("...");
		binding.mtnList.setAdapter(mtn);
        binding.mtnList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick (AdapterView parent, View view, int position, long id) {
				select = 1;
				chooseFile(new File("/sdcard"));
			}
		});

		listView3 = (ListView)findViewById(R.id.list3);

		l = new ArrayList<>();
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
						binding.redSeek.setProgress((int)(256 * Float.parseFloat(color[0])));
                        binding.greenSeek.setProgress((int)(256 * Float.parseFloat(color[1])));
                        binding.blueSeek.setProgress((int)(256 * Float.parseFloat(color[2])));
				}
			}
		}catch(Exception error) {}
    }
	
    public void addList(View v) {
        texture.add("...");
    }
	
	private void save() {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter( Const.getSaveFileDir( getApplicationContext())));
			
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
			bw.write((float)binding.redSeek.getProgress() / 256 + ", " + (float)binding.greenSeek.getProgress() / 256 + ", " + (float)binding.blueSeek.getProgress() / 256);
			bw.close();
			
			Toast.makeText(this, "save", Toast.LENGTH_SHORT).show();
		}catch(Exception error) {Toast.makeText(this, error+"", Toast.LENGTH_SHORT).show();}
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
			alert.show();}catch(Exception e) {Toast.makeText(this, e+"",Toast.LENGTH_SHORT).show();}
		}
	}
}
