package com.internousdev.espresso.action;

import java.util.Map;

import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.ActionSupport;

public class GoLoginAction extends ActionSupport implements SessionAware {
	private String cartFlg;
	private Map<String,Object> session;


	//ログインボタン押下時にログイン画面へ遷移
	public String execute() {
		if(cartFlg != null) { 				 //カートフラグにデータが入っている場合
			session.put("cartFlg", cartFlg); //cartFlgとしてセッションへ追加
		}
		return SUCCESS; 					 //ログイン画面(login.jsp)画面へ遷移
	}

	public String getCartFlg() {
		return cartFlg;
	}

	public void setCartFlg(String cartFlg) {
		this.cartFlg = cartFlg;
	}

	public Map<String,Object> getSession(){
		return this.session;
	}

	public void setSession(Map<String,Object> session) {
	    this.session= session;
	}
}

