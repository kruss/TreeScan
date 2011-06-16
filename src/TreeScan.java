import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;


public class TreeScan {
	
	private static final String APP_NAME = TreeScan.class.getSimpleName()+" - 0.1.0";
	private enum Resolution{
		B, KB, MB, GB
	}
	
	public static void main(String[] args) {
		
		if(args.length == 0){
			print("args: [path (string)] <[resolution (B|KB|MB|GB)]> <[limit (int)]> <[full (boolean)]>");
			System.exit(-1);
		}else{
			try{
				File folder = null;
				Resolution resolution = Resolution.KB;
				long limit = 0;
				boolean full = false;

				if(args.length > 0){
					folder = new File(args[0]);
				}
				if(args.length > 1){
					String arg = args[1];
					if(arg.equalsIgnoreCase(Resolution.B.toString())){
						resolution = Resolution.B;
					}else if(arg.equalsIgnoreCase(Resolution.KB.toString())){
						resolution = Resolution.KB;
					}else if(arg.equalsIgnoreCase(Resolution.MB.toString())){
						resolution = Resolution.MB;
					}else if(arg.equalsIgnoreCase(Resolution.GB.toString())){
						resolution = Resolution.GB;
					}else{
						throw new Exception("Invalid resolution: "+arg);
					}
				}
				if(args.length > 2){
					String arg = args[2];
					limit = (new Long(arg).longValue());
					if(limit < 0){
						throw new Exception("Invalid limit: "+arg);
					}
				}
				if(args.length > 3){
					full = (new Boolean(args[3]).booleanValue());
				}

				info(APP_NAME);
				TreeScan scaner = new TreeScan(folder, resolution, limit, full);
				scaner.scan();
				info("done");
				System.exit(0);
			}catch(Exception e){
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	private static void info(String text) {
		System.out.println("\n\t>>> "+text+" <<<\n");
	}
	
	private static void print(String text) {
		System.out.println(text);
	}

	private File folder;
	private Resolution resolution;
	private long limit;
	private boolean full;
	
	private ArrayList<ScanInfo> scans;

	public TreeScan(File folder, Resolution resolution, long limit, boolean full){
		this.folder = folder;
		this.resolution = resolution;
		this.limit = limit;
		this.full = full;
		
		scans = new ArrayList<ScanInfo>();
	}
	
	private void scan() throws Exception {
		print("scan: "+folder.getAbsolutePath()+(full ? " (full)" : ""));
		scanFolder(folder);
		Collections.sort(scans);
		for(int i=0; i<scans.size(); i++){
			ScanInfo scan = scans.get(i);
			long size = scan.size;
			if(size >= (limit * getResolutionUnit())){
				print(
						getRelativePath(scan.path)+" => "+getResolutionValue(size)+" "+resolution.toString()+
						" ("+scan.folders+" folders, "+scan.files+" files)"
				);
			}else{
				print("=> "+(scans.size() - (i+1))+" items below "+limit+" "+resolution.toString());
				break;
			}
		}
	}

	private String getRelativePath(String path) {
		if(path.equals(folder.getAbsolutePath())){
			return "."+File.separator;
		}else{
			return path.replace(folder.getAbsolutePath(), ".");
		}
	}

	private String getResolutionValue(long size) throws Exception {
		
		DecimalFormat format = new DecimalFormat("0.##");
		double value = (double)size / (double)getResolutionUnit();
		return format.format(value);
	}
	
	private long getResolutionUnit() throws Exception {

		if(resolution == Resolution.B){
			return 1;
		}else if(resolution == Resolution.KB){
			return 1024;
		}else if(resolution == Resolution.MB){
			return 1024 * 1024;
		}else if(resolution == Resolution.GB){
			return 1024 * 1024 * 1024;
		}
		throw new Exception("Undefined resolution: "+resolution.toString());
	}

	private ScanInfo scanFolder(File folder) throws Exception {
		if(folder != null && folder.isDirectory()){
			ScanInfo scan = new ScanInfo(folder);
			for(File file : folder.listFiles()){
				if(file.isDirectory() && (!file.getName().startsWith(".") || full)){
					scan.folders++;
					ScanInfo child = scanFolder(file);
					scan.folders += child.folders;
					scan.files += child.files;
					scan.size += child.size;
				}else{
					long size = file.length();
					scan.files++;
					scan.size += size;
				}
			}
			scans.add(scan);
			return scan;
		}else{
			throw new Exception("Not a folder: "+folder.getAbsolutePath());
		}
	}
	
	class ScanInfo implements Comparable<ScanInfo> {

		public String path;
		public long size;
		public int files;
		public int folders;
		
		public ScanInfo(File folder) {
			path = folder.getAbsolutePath();
			size = 0;
			files = 0;
			folders = 0;
		}

		@Override
		public int compareTo(ScanInfo o) {
			if(size < o.size){
				return 1;
			}else if(size > o.size){
				return -1;
			}else{
				return 0;
			}
		}
	}
}
