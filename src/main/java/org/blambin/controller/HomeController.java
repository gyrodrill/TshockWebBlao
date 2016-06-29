package org.blambin.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
	 * @category TODO:
	 * @throws 
	 * @param session
	 * @param request
	 * void
	 */
	public void UserAndGroupManager(HttpSession session,HttpServletRequest request){
		
		
		
		//用户信息
		request.setAttribute("user", showUserList(session, request, 1)); //装入第一页码的用户信息
		
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
	public  @ResponseBody Map<String, Object> showUserList(HttpSession session,HttpServletRequest request ,int index){
		RestServer rs = (RestServer) session.getAttribute("rs");
		rs.setServerToken();
		JSONObject jo = rs.showUserList();
		
		int pageSize = 10; //每页显示条数
		if (jo.getInt("status") == ErrorCode.ServerOK) {
			JSONArray ja = jo.getJSONArray("users");
			JSONArray newja = new JSONArray();
			
			int pageindex = (index - 1) * pageSize; //当前页面的起始条目id
			
			
			//循环选择json数据
			for (int i = pageindex,j = pageindex; i < (j + pageSize); i++) {
				try {
					newja.put(ja.get(i));
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
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
	 * @category TODO:
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
	
	
	@RequestMapping("/getplayerdetail")
	public  @ResponseBody Map<String, Object> getPlayerDetail(HttpSession session,HttpServletRequest request,@RequestParam("player") String username){
		
		RestServer rs = (RestServer) session.getAttribute("rs");
		rs.setServerToken();
		JSONObject jo = rs.getPlayerDetail(username);
		
		return JSONHelper.jsonToMap(jo);
		
	}
}