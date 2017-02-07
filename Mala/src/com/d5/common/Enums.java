package com.d5.common;

public class Enums {
	public enum SeedsTypeEnum {
		ALL, ADD;
	}
	
	public enum JsonColums{
        Url("url"), ExecuteImplClassName("executeImplClassName"), HtmlSources("htmlSources"),
        DataDealImplClassName("dataDealImplClassName"), InitData("InitData");

        private String value;
        JsonColums(String value){
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }
}
