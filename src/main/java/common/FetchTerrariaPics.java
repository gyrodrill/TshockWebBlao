package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.http.HttpSession;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FetchTerrariaPics {

	static String rootUrl = "http://terraria.gamepedia.com/Item_IDs_Part";
	static int pageNumber = 1;
	static String html = "";
	
	public static void getItemsPics(HttpSession session){
		
		String realPath = session.getServletContext().getRealPath("");
		String filedir = "images/item";
		String fullPath = realPath + filedir;
		
		while (html != null) {
			html = SendHttpGet.sendGet(rootUrl + pageNumber);
			pageNumber ++;
			Document doc = Jsoup.parse(html);
			Element em = doc.getElementById("mw-content-text");
			Element listTable = em.child(1);
			
			Elements list = listTable.child(0).children();
			list.remove(0);
			
			//取出每个物品
			for (Element element : list) {
				
				String picUrl = element.select("img").attr("src");//图片地址
				String picname = element.select("img").attr("alt").replace(" ", "_"); //图片名字；
				String itemId = element.child(1).html(); //item id
				
				File path = new File(fullPath);	
				
				if(!path.isDirectory()){
					path.mkdir();
				}
				
				try {
					
					FileOutputStream fos = new FileOutputStream(path +"/"+ itemId + ".png" );
					//String pic = SendHttpGet.sendGet(picUrl);
					URL url = new URL(picUrl);
					URLConnection uc = url.openConnection();
					InputStream fis = uc.getInputStream();
					
					byte[] bs = new byte[1024];
					
					int len;
					while (( len = fis.read(bs)) != -1 ) {
						fos.write(bs, 0, len);
					}
					
					fos.flush();
					fos.close();
					fis.close();
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				
				
				
			}
			System.out.println(list);
		}
		pageNumber = 1;
	}
	
	public static void main(String[] args) {
		FetchTerrariaPics.getItemsPics(null);
		
	}
}
