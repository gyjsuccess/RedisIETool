package com.d5.demos;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

public class Demo002 {

	public static void main(String[] args) {
		List<A> lis = Lists.newArrayList();
		
		for(int ix=0; ix<10; ix++){
			lis.add(new A(String.valueOf(ix), ix%2==0?null:String.valueOf(ix)));
		}
		
		List<A> lis_ = Lists.newArrayList();
		
		System.out.println(lis.toString());
		
		String rownotnullcol = "name";
		for(A a : lis){
			if(StringUtils.isNotBlank(rownotnullcol)){
				if(StringUtils.isNotBlank(a.getName())){
					lis_.add(a);
				}else{
					continue;
				}
			}else{
				lis_.add(a);
			}
			a.setName(StringUtils.join(a.getName(), "New"));
		}
		lis = lis_;
		
		System.out.println(lis.toString());
	}
}


class A {
	private String id;
	private String name;
	public A(){
	}
	public A(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		return "A [id=" + id + ", name=" + name + "]";
	}
}