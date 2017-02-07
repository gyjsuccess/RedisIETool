package com.d5.tool;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.d5.common.Constants;
import com.d5.util.MongodbUtil;

public class SelfDefineTools {
	public static void writeRefInfo2Mongo() {
		String[][] refArrs = {
				/*{"家庭成员", "familyMember", ""},
				{"更多中文名", "moreChineseName", ""},
				{"更多外文名", "moreForeignName", ""},
				{"职业", "profession", ""},
				{"出生地", "birthArea", ""},
				{"出生日期", "birthday", ""},
				{"星座", "constellatory", ""},
				{"性别", "sex", ""},
				{"官方网站", "officialSite", ""},
				{"编剧", "writer", ""},
				{"主演", "cast", ""},
				{"制片国家/地区", "countryArea", ""},
				{"语言", "language", ""},
				{"上映日期", "issue", ""},
				{"IMDb链接", "imdbLink", ""},
				{"又名", "anotherName", ""},
				{"片长", "duration", ""},
				{"首播", "issue", ""},
				{"集数", "episodeCount", ""},
				{"单集片长", "duration", ""},
				{"类型", "type", ""},
				{"导演", "director", ""}*/
				/*{"imdb编号", "imdbNum", ""}*/
				/*{"生卒日期", "birthAndDeath", ""}*/
				{"季数", "seasonCount", ""},
				{"官方小站", "officialLittleSite", ""}
		};
		List<Document> documents = new ArrayList<Document>();
		String[] colNameArr = {"chineseName", "englishName", "url"};
		for(String[] refArr : refArrs){
			Document doc = new Document();
			int ix = 0;
			for(String colNam : colNameArr){
				doc.append(colNam, refArr[ix]);
				ix ++;
			}
			documents.add(doc);
		}
		MongodbUtil.insertMany(Constants.ATTR_RELA_COLLECT, documents);
	}
}
