package com.kyhsgeekcode.rootpicker;

import android.app.*;
import android.content.*;
import android.content.res.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.stericson.RootTools.*;
import java.io.*;
import java.util.*;

public class FileSelectorActivity extends ListActivity
{
	private List<String> item = (List<String>) null;
	private List<String> path = (List<String>) null;
	private String root = "/";
	private TextView mPath;
	String lspath="";

	private String TAG="RootPicker";
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fileaselactivity);
		mPath = (TextView) findViewById(R.id.path);
		String [] abis=android.os.Build.SUPPORTED_ABIS;
		String binary=null;
		AssetManager asm=getAssets();
		for (String abi:abis)
		{
//			armeabi
//			armeabi-v7a
//			armeabi-v7a-hard
//			arm64-v8a
//			x86
//			x86_64
//			mips
//			mips64	 
			if (abi.contains("armeabi") || abi.contains("arm64"))
			{
				binary = "ls-arm";
				break;
			}
			else if (abi.contains("x86"))
			{
				binary = "ls-x86";
				break;
			}
		}
		try
		{
			InputStream is= asm.open(binary);
			File dir=getFilesDir();
			File dest=new File(dir, "/ls.bin");
			FileOutputStream fos=new FileOutputStream(dest);
			byte[] data=new byte[2048];
			int read=0;
			while ((read = is.read(data, 0, 2048)) > 0)
			{
				fos.write(data, 0, read);
			}
			is.close();
			fos.flush();
			fos.close();
			lspath = dest.getAbsolutePath();
			RootTools.runBinary(this, "chmod", "777 " + lspath);
		}
		catch (IOException e)
		{
			Toast.makeText(this, "Failed to copy ls", 3).show();
		}
		getDir(root);
	}

	private void getDir(String dirPath)
	{
		mPath.setText("Location: " + dirPath);
		item = new ArrayList<String>();
		path = new ArrayList<String>();
		File f = new File(dirPath);
		File[] files = f.listFiles();
		if (!dirPath.equals(root))
		{
			item.add(root);
			path.add(root);
			item.add("../");
			path.add(f.getParent());
		}

		for (int i = 0; i < files.length; i++)
		{
			File file = files[i];
			path.add(file.getPath());
			if (file.isDirectory())
				item.add(file.getName() + "/");
			else
				item.add(file.getName());
		}
		RefreshList();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		final File file = new File(path.get(position));
		if (file.isDirectory())
		{
			if (file.canRead())
				getDir(path.get(position));
			else
			{
				getDirRoot(path.get(position));
				/**/
			}
		}
		else
		{
			new AlertDialog.Builder(this)
				.setIcon(R.drawable.ic_launcher)
				.setTitle("[" + file.getName() + "]")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which)
					{
						// TODO Auto-generated method stub
						Intent result = new Intent("com.kyunggi.renamer.RESULT_ACTION");
						result.putExtra("com.kyunggi.renamer.path", file.getAbsolutePath());
						setResult(Activity.RESULT_OK, result);
						finish();
					}
				}).show();
		}
	}

	private void getDirRoot(String dirPath)
	{
		if (RootTools.isRootAvailable())
		{
			while (!RootTools.isAccessGiven())
			{
				RootTools.offerSuperUser(this);
			}
			List<DirEnt> entries= runLs(dirPath);
			mPath.setText("Location: " + dirPath);
			item = new ArrayList<String>();
			path = new ArrayList<String>();
			if (!dirPath.equals(root))
			{
				item.add(root);
				path.add(root);
				item.add("../");
				path.add(new File(dirPath).getParent());
			}

			for (int i = 0; i < entries.size(); i++)
			{
				DirEnt file = entries.get(i);
				path.add(dirPath + "/" + file.name);
				if (file.isDirectory())
					item.add(file.getName() + "/");
				else
					item.add(file.getName());
			}
			RefreshList();
		}
		else
		{
			new AlertDialog.Builder(this)
				.setIcon(R.drawable.ic_launcher)
				.setTitle("[" + dirPath + "] folder can't be read!")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which)
					{
					}
				}).show();
		}
		return ;
	}

	private void RefreshList()
	{
		ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, R.layout.row, item);
		fileList.sort(new Comparator<String>(){
				@Override
				public int compare(String p1, String p2)
				{
					if (p1.equals("/"))
					{
						return -1;
					}		
					if (p1.equals("..") || p1.equals("../"))
					{
						return -1;
					}
					if(p2.equals("/"))
					{
						return 1;
					}
					if (p2.equals("..") || p2.equals("../"))
					{
						return 1;
					}
					if (p1.endsWith("/") && !p2.endsWith("/"))
					{
						return -1;
					}
					if (p2.endsWith("/") && !p1.endsWith("/"))
					{
						return 1;
					}
					return p1.compareTo(p2);
				}		
			});
		Collections.sort(path, new Comparator<String>(){
				@Override
				public int compare(String p1, String p2)
				{
					String n1=new File(p1).getName();
					String n2=new File(p2).getName();
					if (n1.equals("/"))
					{
						return -1;
					}
					if (n1.equals("..") || n1.equals("../"))
					{
						return -1;
					}
					if(n2.equals("/"))
					{
						return 1;
					}
					if (n2.equals("..") || n2.equals("../"))
					{
						return 1;
					}
					if ((p1.endsWith("/")||new File(p1).isDirectory()) && !(p2.endsWith("/")||new File(p2).isDirectory()))
					{
						return -1;
					}
					if ((p2.endsWith("/") ||new File(p2).isDirectory())&& !(p1.endsWith("/")||new File(p1).isDirectory()))
					{
						return 1;
					}
					return n1.compareTo(n2);
				}
			});
		setListAdapter(fileList);
	}
	private List<DirEnt> runLs(String path)
	{
		List<DirEnt> ents=new ArrayList<>();
		try
		{
			ProcessBuilder builder = new ProcessBuilder("su");
			builder.redirectErrorStream(true);
			java.lang.Process shProcess = builder.start();
			DataOutputStream os = new DataOutputStream(shProcess.getOutputStream());
			DataInputStream osRes = new DataInputStream(shProcess.getInputStream());
			BufferedReader reader = new BufferedReader(new InputStreamReader(osRes));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));

			// osErr = new DataInputStream(shProcess.getErrorStream());

			if (null != os && null != osRes)
			{								
				writer.write("((" + lspath + " " + path + ") && echo --EOF--) || echo --EOF--\n");
				writer.flush();
				String answer="";
				//String error="";
				String tmp;
				Log.d(TAG, "DOING");
				tmp = reader.readLine();
				int i=0;
				String name = "";
				//String [] parsed=new String[2];
				while (tmp != null && ! tmp.trim().equals("--EOF--"))
				{
					//answer += tmp;//System.out.println ("Stdout: " + tmp);
					Log.d(TAG, "" + tmp);
					if (i % 2 == 0)
					{
						name = tmp;		
					}
					else
					{
						DirEnt de=new DirEnt();
						de.name = name;
						try
						{
							de.type = Integer.parseInt(tmp);
						}
						catch (NumberFormatException e)
						{
							Log.e(TAG, "", e);
						}
						if (!de.name.equals(".") && !de.name.equals(".."))
							ents.add(de);
					}
					//Log.d(TAG,Arrays.toString(tmp.getBytes()));
					tmp = reader.readLine();
					++i;
				}
				Log.d(TAG, "Done");
				/*String [] lines=answer.split("(\r\n|\r|\n)", 0);
				 for (int i=0;i < lines.length/2;++i)
				 {
				 Log.d(TAG,"i"+i);
				 String name=lines[i*2].trim();
				 String c=lines[i*2+1].trim();
				 int character=0;
				 try{
				 character=Integer.parseInt(c);
				 DirEnt de=new DirEnt();
				 de.name=name;
				 de.type=character;
				 ents.add(de);
				 }catch(NumberFormatException nfe)
				 {
				 Log.e(TAG,"",nfe);
				 }
				 }	*/	
			}
		}
		catch (IOException e)
		{
			Log.e(TAG, "", e);
		}
		return ents;
	}

	class DirEnt
	{
		String name;
		int type;

		public boolean isDirectory()
		{		
			return type == 4;
		}

		public String getName()
		{
			return name;
		}
	}
	@Override
	public void onBackPressed()
	{
		String path=mPath.getText().toString().replaceAll("Location :", "");
		if (root.equals(path))
		{
			super.onBackPressed();
		}
		else
		{
			File file=new File(path).getParentFile();
			if (file.isDirectory())
			{
				if (file.canRead())
					getDir(file.getPath());
				else
				{
					getDirRoot(file.getPath());
					/**/
				}
			}
		}
		return ;
	}

}
