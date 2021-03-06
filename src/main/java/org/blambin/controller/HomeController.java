package org.blambin.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.blambin.common.ColorUtil;
import org.blambin.common.ContentType;
import org.blambin.common.ErrorCode;
import org.blambin.common.JSONHelper;
import org.blambin.common.RestServer;
import org.blambin.common.ServerCommon;
import org.blambin.entity.Server;
import org.blambin.service.IServerService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/home")
public class HomeController {

	
	@Autowired
	private IServerService serverService;
	
	@Autowired
	private ServerCommon serverCommon;
	
	/***
	 * 根据 contentid 控制如何跳转页面
	 * @param session
	 * @param contentid
	 * @param ra
	 * @param request
	 * @return
	 */
	
	@RequestMapping("/content")
	public String selectBodyContent(HttpSession session,Integer contentid,RedirectAttributes ra,HttpServletRequest request){
		
		session.setAttribute("contentid", contentid);
		
		if (contentid == ContentType.AddServer) {
			
		}
		if (contentid == ContentType.ServerDetail){
			 ServerDetail(session, contentid, request);
		}
		if (contentid == ContentType.ServerBaseCommand) {
			ServerBaseCommand(session, request);
		}
		if (contentid == ContentType.UserAndGroupManager) {
			UserAndGroupManager(session, request);
		}
		
		
		return "main";
		
	}
	
	/***
	 * 服务器详情单页
	 * @param session
	 * @param contentid
	 * @param request
	 */
	
	public void ServerDetail(HttpSession session,Integer contentid,HttpServletRequest request){
		Server server = new Server();
		server.setId(Integer.parseInt((String) request.getParameter("serverid")));
		
		// 设置当前服务器并设置token
		
		serverCommon.SetServerInSession(serverService.queryServerByServerId(server), session);
		
		RestServer rs = new RestServer(session);
		if (null==rs.getToken() || ""== rs.getToken()) {
			rs.setServerToken();
		}
		
		session.setAttribute("rs", rs);
	}
	
	/***
	 * 服务器命令页面
	 * @param session
	 * @param request
	 */
	public void ServerBaseCommand(HttpSession session,HttpServletRequest request){
		
		RestServer rs = (RestServer) session.getAttribute("rs");
		//设置临牌
		JSONObject getTokenstatus = rs.setServerToken();
		//提取基本消息
		JSONObject jo = rs.status();
		
		if (getTokenstatus != null) {
			
			//正常登陆/正常创建token
			if (jo.getInt("status") == ErrorCode.ServerOK) {
				request.setAttribute("tokenstatus",JSONHelper.jsonToMap(getTokenstatus));
			}else if (jo.getInt("tokenstatus") == ErrorCode.ServerUnreach) {
				//传入错误信息
				request.setAttribute("tokenstatus",JSONHelper.jsonToMap(getTokenstatus));
			}else if (jo.getInt("tokenstatus") == ErrorCode.ErrorAPI) {
				//传入错误信息
				request.setAttribute("tokenstatus",JSONHelper.jsonToMap(getTokenstatus));
			}else if (jo.getInt("tokenstatus") == ErrorCode.TokenUnvalid) {
				//传入错误信息
				request.setAttribute("tokenstatus",JSONHelper.jsonToMap(getTokenstatus));
			}else if (jo.getInt("tokenstatus") == ErrorCode.URLError) {
				//传入错误信息
				request.setAttribute("tokenstatus",JSONHelper.jsonToMap(getTokenstatus));
			}else if (jo.getInt("tokenstatus") == ErrorCode.UnKnownError) {
				//传入错误信息
				request.setAttribute("tokenstatus",JSONHelper.jsonToMap(getTokenstatus));
			}
		}
		
		
 		if (jo != null) {
			
			//只有没错误的情况下才显示服务器信息
			if (jo.getInt("status") == ErrorCode.ServerOK) {
				request.setAttribute("status",JSONHelper.jsonToMap(rs.status()));
			}else if (jo.getInt("status") == ErrorCode.ServerUnreach) {
				//传入错误信息
				request.setAttribute("status",JSONHelper.jsonToMap(jo));
			}else if (jo.getInt("status") == ErrorCode.ErrorAPI) {
				//传入错误信息
				request.setAttribute("status",JSONHelper.jsonToMap(jo));
			}else if (jo.getInt("status") == ErrorCode.TokenUnvalid) {
				//传入错误信息
				request.setAttribute("status",JSONHelper.jsonToMap(jo));
			}else if (jo.getInt("status") == ErrorCode.URLError) {
				//传入错误信息
				request.setAttribute("status",JSONHelper.jsonToMap(jo));
			}else if (jo.getInt("status") == ErrorCode.UnKnownError) {
				//传入错误信息
				request.setAttribute("status",JSONHelper.jsonToMap(jo));
			}
		}	
		
	}
	
	/***
	 * 用户和组管理详情页面
	 * @author blambin
	 * @since 2016年6月25日
	 * @throws 
	 * @param session
	 * @param request
	 * void
	 */
	public void UserAndGroupManager(HttpSession session,HttpServletRequest request){
		
		
		
		//用户信息
		request.setAttribute("user", showUserList(session, request, 1, "")); //装入第一页码的用户信息
		
		////所有在线用户信息
		Map<String, Object> activeUsersMap = showActivePlayers(session, request);
		JSONObject jo = new JSONObject(activeUsersMap);
		
		//所有在线用户的用户名列表
		List<String> usernames = new ArrayList<String>();
		JSONArray players =  jo.getJSONArray("players");
		
		for (int i = 0; i < players.length(); i++) {
			usernames.add(((JSONObject)players.get(i)).getString("nickname"));
		}
		
		//根据在线用户列表查询出每个用户的详细信息
		
		JSONObject newjo = new JSONObject();
		JSONArray newplayers = new JSONArray();
		
		for (String username : usernames) {
			
			getPlayerDetail(session, request, username);
			
			newplayers.put(getPlayerDetail(session, request, username));
		}

		newjo.putOnce("status", 200);
		newjo.put("players", newplayers);
		request.setAttribute("onlineUser", JSONHelper.jsonToMap(newjo));
		
		//获取所有组
		
		Map<String, Object> teamListMap = getTeamList(session, request);
		request.setAttribute("teamList", teamListMap);
	}
	
	
	/***
	 * 调用命令功能，返回JSON
	 * @param session
	 * @param cmd
	 * @param request
	 * @return
	 */
	
	@RequestMapping("/rawcmd")
	public @ResponseBody Map<String, Object> rawcmd(HttpSession session,String cmd,HttpServletRequest request){
		RestServer rs = (RestServer) session.getAttribute("rs");
		rs.setServerToken();

		return JSONHelper.jsonToMap(rs.rawcmd(cmd));
	}
	
	/***
	 * 广播功能,返回 JSON
	 * @param session
	 * @param msg
	 * @param request
	 * @return
	 */
	@RequestMapping("/broadcast")
	public  @ResponseBody Map<String, Object> broadcast(HttpSession session,String msg,HttpServletRequest request){
		RestServer rs = (RestServer) session.getAttribute("rs");
		rs.setServerToken();
		return JSONHelper.jsonToMap(rs.broadcast(msg));
	}
	
	@RequestMapping("/showuserlist")
	public  @ResponseBody Map<String, Object> showUserList(HttpSession session,HttpServletRequest request ,int index,String key){
		RestServer rs = (RestServer) session.getAttribute("rs");
		rs.setServerToken();
		JSONObject jo = rs.showUserList();
		
		//
		
		int pageSize = 10; //每页显示条数
		if (jo.getInt("status") == ErrorCode.ServerOK) {
			
			JSONArray allja =  jo.getJSONArray("users");//取出所有
			
			//过滤空串
			String newkey = ((key == null)?"":key);
			
			//取出搜索匹配KEy的数据
			JSONArray ja = new JSONArray();
			for (Object object : allja) {
				String name = (String) ((JSONObject)object).get("name");
				if(name.contains(newkey)){
					ja.put(object);
				}
			}
			
			//分页开始
			JSONArray newja = new JSONArray();

			int pageindex = (index - 1) * pageSize; //当前页面的起始条目id
			
			
			//循环选择json数据
			for (int i = pageindex,j = pageindex; i < (j + pageSize); i++) {
				try {
					newja.put(ja.get(i));
					
				} catch (JSONException e) {
					e.printStackTrace();
					break; //如果报错是就跳出循环
				}
			}
			//分页基本信息
			int jsonLength = ja.length(); //json的长度
			int pageTotal = ((jsonLength % pageSize) > 0)?((jsonLength / pageSize) + 1):(jsonLength / pageSize); //总页数
			
			JSONObject pageJsonObject = new JSONObject();
			pageJsonObject.put("jsonlength", jsonLength);
			pageJsonObject.put("pagetotal", pageTotal);
			pageJsonObject.put("index", index);
			
			//构建分页后的jsonobject对象
			JSONObject newjo = new JSONObject();
			
			newjo.put("status", 200);
			newjo.put("users", newja);
			newjo.put("pageinfo", pageJsonObject);
			
			return JSONHelper.jsonToMap(newjo);
		}
		return JSONHelper.jsonToMap(jo);
	}
	
	
	/***
	 * 获取当前所有在线玩家列表 
	 * @author blambin
	 * @since 2016年6月25日
	 * @throws 
	 * @param session
	 * @param request
	 * @return
	 * Map<String,Object>
	 */
	@RequestMapping("/showactiveplayers")
	public  @ResponseBody Map<String, Object> showActivePlayers(HttpSession session,HttpServletRequest request){
		
		RestServer rs = (RestServer) session.getAttribute("rs");
		rs.setServerToken();
		JSONObject jo = rs.getActivePlayers();
		
		return JSONHelper.jsonToMap(jo);
		
	}
	
	
	/***
	 * 获取用户详细信息
	 * @author blambin
	 * @since 2016年7月1日
	 * @throws 
	 * @param session
	 * @param request
	 * @param username
	 * @return
	 * Map<String,Object>
	 */
	@RequestMapping("/getplayerdetail")
	public  @ResponseBody Map<String, Object> getPlayerDetail(HttpSession session,HttpServletRequest request,@RequestParam("player") String username){
		
		RestServer rs = (RestServer) session.getAttribute("rs");
		rs.setServerToken();
		JSONObject jo = rs.getPlayerDetail(username);
		
		return JSONHelper.jsonToMap(jo);
		
	}
	

	/***
	 * 获取所有用户组信息
	 * @author blambin
	 * @since 2016年7月2日
	 * @throws 
	 * @param session
	 * @param request
	 * @return
	 * Map<String,Object>
	 */
	@RequestMapping("/getTeamList")
	public  @ResponseBody Map<String, Object> getTeamList(HttpSession session,HttpServletRequest request){
		
		RestServer rs = (RestServer) session.getAttribute("rs");
		rs.setServerToken();
		JSONObject jo = rs.getTeamList();
		
		return JSONHelper.jsonToMap(jo);
		
	}
	
	/***
	 * 
	 * @author blambin
	 * @since 2016年7月2日
	 * @throws 
	 * @param session
	 * @param request
	 * @param args
	 * @return
	 * Map<String,Object>
	 */
	@RequestMapping("/createuser")
	public  @ResponseBody Map<String, Object> createUser(HttpSession session,HttpServletRequest request){
		
		String user = (null == request.getParameter("user")?"":request.getParameter("user"));
		String group = (null == request.getParameter("group")?"":request.getParameter("group"));
		String password = (null == request.getParameter("password")?"":request.getParameter("password"));
		
		
		RestServer rs = (RestServer) session.getAttribute("rs");
		rs.setServerToken();
		
		JSONObject param = new JSONObject();
		
		param.put("user", user);
		param.put("group", group);
		param.put("password", password);
		
		JSONObject jo = rs.createUser(param);
		
		return JSONHelper.jsonToMap(jo);
		
	}
	
	/***
	 * 删除用户
	 * @author blambin
	 * @since 2016年7月2日
	 * @throws 
	 * @param session
	 * @param request
	 * @param user
	 * @return
	 * Map<String,Object>
	 */
	@RequestMapping("/deleteuser")
	public  @ResponseBody Map<String, Object> deleteuser(HttpSession session,HttpServletRequest request,String user){
		RestServer rs = (RestServer) session.getAttribute("rs");
		rs.setServerToken();
		JSONObject jo = rs.deleteUser(user);
		return JSONHelper.jsonToMap(jo);
	}
	
	/***
	 *  更新用户信息
	 * @author blambin
	 * @since 2016年7月2日
	 * @throws 
	 * @param session
	 * @param request
	 * @return
	 * Map<String,Object>
	 */
	@RequestMapping("/updateuser")
	public  @ResponseBody Map<String, Object> updateuser(HttpSession session,HttpServletRequest request){
		
		String user = (null == request.getParameter("user")?"":request.getParameter("user"));
		String group = (null == request.getParameter("group")?"":request.getParameter("group"));
		String password = (null == request.getParameter("password")?"":request.getParameter("password"));
		
		RestServer rs = (RestServer) session.getAttribute("rs");
		rs.setServerToken();
		
		JSONObject param = new JSONObject();
		
		param.put("user", user);
		param.put("group", group);
		param.put("password", password);
		
		JSONObject jo = rs.updateUser(param);
		if (jo.has("group-response")) {
			jo.put("groupresponse", jo.get("group-response"));
		}
		if (jo.has("password-response")) {
			jo.put("passwordresponse", jo.get("password-response"));
		}
		
		
		return JSONHelper.jsonToMap(jo);
		
	}
	
	/**
	 * 获取日志
	 * @author blambin
	 * @since 2016年7月3日
	 * @throws 
	 * @param session
	 * @param request
	 * @return
	 * Map<String,Object>
	 */
	@RequestMapping("/getLog")
	public  @ResponseBody Map<String, Object> getLog(HttpSession session,HttpServletRequest request,int count){
		
		RestServer rs = (RestServer) session.getAttribute("rs");
		rs.setServerToken();
		
		JSONObject jo = rs.getLog(count);
		
		return  JSONHelper.jsonToMap(jo);
		
	}
	
	/***
	 * 聊天功能 
	 * @author blambin
	 * @since 2016年7月8日
	 * @throws 
	 * @param session
	 * @param request
	 * @return
	 * @throws ParseException
	 * Map<String,Object>
	 */
	@RequestMapping("/chat")
	public  @ResponseBody Map<String, Object> chat(HttpSession session,HttpServletRequest request) throws ParseException{
		
		RestServer rs = (RestServer) session.getAttribute("rs");
		rs.setServerToken();
		//获取完全日志
		JSONObject jo = rs.getLog(1000);
	
		//待返回的json
		JSONObject newjo = new JSONObject();
		
		String time;
		String chatcontent;
		
		String[] timeCache;
		String[] chatContentCache;
		
		
		//有log表示有内容或者有api
		if (jo.has("log")) {
			JSONArray ja = jo.getJSONArray("log");
			
			//状态
			newjo.put("status", "200");
			//多条array记录
			JSONArray mutiChat = new JSONArray();
			
			//单条记录
			JSONObject oneChat;
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			for (Object log : ja) {
				 String a = (String)log;
				 
				 //如果符合聊天内容的需求
				 if (a.contains("Utils: INFO:")) {
					 
					 //取出时间
					 timeCache = a.split(" - ");
					 time = timeCache[0];
					 
					 //取出聊天内容(最后字符一个对象)
					 chatContentCache = a.split("Utils: INFO:");
					 
					 chatcontent = chatContentCache[(chatContentCache.length - 1)].replace("服务器广播: ", "");
					 
					 //如果聊天检查点为空，则全部放到json里
					 if (session.getAttribute("chatstatuspoint") == null) {
						 
						 
						 oneChat = new JSONObject();
						 oneChat.put("log", chatcontent);
						 oneChat.put("time", time);
						 
						 mutiChat.put(oneChat);
						 //System.out.println(a);
						 
					}else {
						
						String chatstatuspoint = (String) session.getAttribute("chatstatuspoint");
						Date pointDate = sdf.parse(chatstatuspoint);
						Date thisDate = sdf.parse(time);
						//如果比这个时间点都大的，就都放进去
						if (thisDate.after(pointDate)) {
							oneChat = new JSONObject();
							oneChat.put("log", chatcontent);
							oneChat.put("time", time);
							mutiChat.put(oneChat);
						}
					}
				}
			}
			
			newjo.put("mutiChat",mutiChat);
			
			Date date = null;
			
			
			for (Object object : mutiChat) {
				String StringTime = (String) ((JSONObject)object).get("time");
				Date currentDate = sdf.parse(StringTime);
				if (date == null) {
					date = currentDate;
				}else if (currentDate.after(date) ) {
					date = currentDate;
				}
			}
			
			if (date != null) {
				String dataString = sdf.format(date);
				session.setAttribute("chatstatuspoint",dataString);	
			}
					
			return JSONHelper.jsonToMap(newjo);
		}
		
		//返回错误信息
		return JSONHelper.jsonToMap(jo);
		
	}
	
	/***
	 * 获取单个用户组功能 
	 * @author blambin
	 * @since 2016年7月8日
	 * @throws 
	 * @param session
	 * @param request
	 * @param groupName
	 * @return
	 * Map<String,Object>
	 */
	
	@RequestMapping("getGroup")
	public @ResponseBody Map<String, Object> getGroup(HttpSession session,HttpServletRequest request,@RequestParam("group") String groupName){
		
		RestServer rs = (RestServer) session.getAttribute("rs");
		rs.setServerToken();
		JSONObject jo = rs.getGroup(groupName);
		
		//把颜色转成Hex表示法
		if (jo.has("chatcolor")) {
			String color = jo.getString("chatcolor");
			String[] Intcolor = color.split(",");
			
			String HexColor = ColorUtil.NumberToHex(Integer.parseInt(Intcolor[0]), Integer.parseInt(Intcolor[1]), Integer.parseInt(Intcolor[2]));
			jo.put("chatcolor", HexColor);
			
		}
		
		return JSONHelper.jsonToMap(jo);
		
	}
	
	/***
	 * 添加组
	 * @author blambin
	 * @since 2016年7月11日
	 * @throws 
	 * @param session
	 * @param request
	 * @return
	 * Map<String,Object>
	 */
	@RequestMapping("createGroup")
	public @ResponseBody Map<String, Object> createGroup(HttpSession session,HttpServletRequest request){
		
		String group = (null == request.getParameter("group")?"":request.getParameter("group"));
		String parent = (null == request.getParameter("parent")?"":request.getParameter("parent"));
		String permissions = (null == request.getParameter("permissions")?"":request.getParameter("permissions"));
		String chatcolor = (null == request.getParameter("chatcolor")?"":request.getParameter("chatcolor"));
		
		//转charcolor的表现格式
		String chatcolorNumber = ColorUtil.HexToNumber(chatcolor.replace("%23", ""));
		
		RestServer rs = (RestServer) session.getAttribute("rs");
		rs.setServerToken();
		
		JSONObject param = new JSONObject();
		param.put("group", group);
		param.put("parent", parent);
		param.put("permissions", permissions);
		param.put("chatcolor", chatcolorNumber);
		
		JSONObject jo = rs.createGroup(param);
		return  JSONHelper.jsonToMap(jo);
		
	}
	
	/***
	 * 更新用户组
	 * @author blambin
	 * @since 2016年7月11日
	 * @throws 
	 * @param session
	 * @param request
	 * @return
	 * Map<String,Object>
	 */
	@RequestMapping("updateGroup")
	public @ResponseBody Map<String, Object> updateGroup(HttpSession session,HttpServletRequest request){
		
		String group = (null == request.getParameter("group")?"":request.getParameter("group"));
		String parent = (null == request.getParameter("parent")?"":request.getParameter("parent"));
		String permissions = (null == request.getParameter("permissions")?"":request.getParameter("permissions"));
		String chatcolor = (null == request.getParameter("chatcolor")?"":request.getParameter("chatcolor"));
		
		//转charcolor的表现格式
		String chatcolorNumber = ColorUtil.HexToNumber(chatcolor.replace("%23", ""));
		
		RestServer rs = (RestServer) session.getAttribute("rs");
		rs.setServerToken();
		
		JSONObject param = new JSONObject();
		param.put("group", group);
		param.put("parent", parent);
		param.put("permissions", permissions);
		param.put("chatcolor", chatcolorNumber);
		
		JSONObject jo = rs.updateGroup(param);
		return  JSONHelper.jsonToMap(jo);
		
	}
	
	/**
	 * 删除用户组
	 * @author blambin
	 * @since 2016年7月12日
	 * @throws 
	 * @param session
	 * @param request
	 * @param groupName
	 * @return
	 * Map<String,Object>
	 */
	@RequestMapping("deleteGroup")
	public @ResponseBody Map<String, Object> deleteGroup(HttpSession session,HttpServletRequest request,@RequestParam("group") String groupName){

		RestServer rs = (RestServer) session.getAttribute("rs");
		rs.setServerToken();
		JSONObject jo = rs.deleteGroup(groupName);
		return  JSONHelper.jsonToMap(jo);
	}
		
	
	
}
