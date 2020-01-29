package com.internousdev.espresso.action;

import java.util.Map;

import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.ActionSupport;

public class LogoutAction extends ActionSupport implements SessionAware {

	private Map<String, Object> session;

	public String execute() {
		String userId = String.valueOf(session.get("userId"));
		//セッションのユーザID保存フラグをString型で変数に代入
		String tempSavedUserIdFlg = String.valueOf(session.get("savedUserIdFlg"));

		//セッションのユーザID保存フラグがnullの場合：false, 入っている場合：true
		boolean savedUserIdFlg = "null".equals(tempSavedUserIdFlg) ? false : Boolean.valueOf(tempSavedUserIdFlg);
		session.clear(); //セッション情報を削除

		//ユーザID保存フラグがtrueの場合（ログイン時、ユーザID保存チェックボックスにチェックを入れていた場合）
		if (savedUserIdFlg) {
			session.put("savedUserIdFlg", savedUserIdFlg); //ユーザーID保存フラグをセッションに保持
			session.put("userId", userId); //ユーザーIDをセッションに保持
		}
		return SUCCESS; //ホーム画面(home.jsp)へ遷移
	}

	public Map<String, Object> getSession() {
		return session;
	}

	public void setSession(Map<String, Object> session) {
		this.session = session;
	}
}
