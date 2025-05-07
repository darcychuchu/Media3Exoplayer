package com.vlog.player.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vlog.player.data.model.VideoItem
import com.vlog.player.data.model.VideoListResponse

@Composable
fun VideoListScreen(onVideoClick: (VideoItem) -> Unit) {
    // 模拟数据 https://66log.com/api/json/v1/videos/list
    val videoListJson = """
        {"data":[{"id":"f3b572ce-c399-49ea-88b0-ee890079a558","categoryId":"83e62001-3ddd-4ee1-85ab-d638896931d9","attachmentId":"c59206dd-46af-4f7e-9c18-bbe76c9b050e","title":"斗破苍穹 年番1","score":"6.8","alias":"","director":"","actors":"","region":"中国","language":"汉语普通话","description":"三年之约后，萧炎终于在迦南学院见到了薰儿，此后他广交挚友并成立磐门；为继续提升实力以三上云岚宗为父复仇，他以身犯险深入天焚炼气塔吞噬陨落心炎……","tags":"","author":"","coverUrl":"https://pic.youkupic.com/upload/vod/20220731-1/87c88da26e74c1e33ba5a5793e453373.jpg","videoPlayList":[]},{"id":"1c062a01-ca15-421a-9f96-02b0c30de7c6","categoryId":"70beafb1-03b9-4d02-9eea-f54249d6f284","attachmentId":"33503f68-b4dc-44c1-a258-ea79a46ccc3d","title":"哪吒之魔童闹海","score":"8.5","alias":"哪吒2,哪吒2之魔童闹海,Ne Zha 2","director":"饺子","actors":"吕艳婷,囧森瑟夫,瀚墨,陈浩,绿绮,张珈铭,杨卫,雨辰,周泳汐,韩雨泽,南屿,零柒,良生","region":"中国","language":"汉语普通话","description":"天劫之后，哪吒、敖丙的灵魂虽保住了，但肉身很快会魂飞魄散。太乙真人打算用七色宝莲给二人重塑肉身。但是在重塑肉身的过程中却遇到重重困难，哪吒、敖丙的命运将走向何方？","tags":"","author":"饺子","coverUrl":"https://pic.youkupic.com/upload/vod/20250130-1/d5adae6679e8f41cae6828ceee01646d.jpg","videoPlayList":[]},{"id":"41513b9a-8c4f-4ea4-af78-2902885e1ad2","categoryId":"83e62001-3ddd-4ee1-85ab-d638896931d9","attachmentId":"f33b58d5-4ec6-40d3-8c9f-72e04e9ca7b6","title":"凡人修仙传","score":"7.9","alias":"凡人修仙传：风起天南","director":"伍镇焯,王裕仁","actors":"钱文青,杨天翔,杨默,歪歪,谷江山,乔诗语,佟心竹","region":"中国","language":"汉语普通话","description":"平凡少年韩立出生贫困，为了让家人过上更好的生活，自愿前去七玄门参加入门考核，最终被墨大夫收入门下。\n墨大夫一开始对韩立悉心培养、传授医术，让韩立对他非常感激，但随着一同入门的弟子张铁失踪，韩立才发现了...","tags":"修仙,仙侠,动画,动漫,玄幻,修真,中国大陆,2020","author":"金增辉,李欣雨","coverUrl":"https://pic.youkupic.com/upload/vod/20211029-1/a4f9edf92a26ba526e562a40a2ed5e17.jpg","videoPlayList":[]},{"id":"ad1805bf-bb14-47b9-b416-1acef8391276","categoryId":"8886c5eb-1ec2-43ee-84f7-35217940992b","attachmentId":"2d54085d-2c5e-489a-9323-3ba06400efd0","title":"雁回时","score":"6.8","alias":"贵女/重生之贵女难求","director":"杨龙","actors":"陈都灵,辛云来,何泓姗,喻恩泰,温峥嵘,王艳,刘旭威,傅菁,黄海冰,廖慧佳","region":"中国","language":"国语","description":"寒冬腊月，从小被弃养乡下的庄家三小姐庄寒雁，竟遍体鳞伤地晕倒在庄府门前，她的出现引起庄家内宅大乱，更惹来大理寺少卿傅云夕的关注和探究。这只冬日北上的孤雁，背后究竟藏着什么秘密？京城奸宦一夜倒台，神秘义...","tags":"","author":"","coverUrl":"https://pic.youkupic.com/upload/vod/20250318-1/9fa28a2ace16152e40054a766c37fc91.jpg","videoPlayList":[]},{"id":"5e3944e0-a017-4f23-892b-71fa8706ed28","categoryId":"83e62001-3ddd-4ee1-85ab-d638896931d9","attachmentId":"4f0f9d49-108f-4591-9693-cfcfd5ab9aeb","title":"剑来 第一季","score":"8.9","alias":"剑来 动画版,Sword of Coming","director":"","actors":"","region":"中国","language":"汉语普通话","description":"大千世界，无奇不有。 骊珠洞天中本该有大气运的贫寒少年，因为本命瓷碎裂的缘故，使得机缘临身却难以捉住。基于此，众多大佬纷纷以少年为焦点进行布局，使得少年身边的朋友获得大机缘，而少年却置身风口浪尖之上…","tags":"","author":"","coverUrl":"https://pic.youkupic.com/upload/vod/20240815-1/345228a8f8a8c10084349ffafafaee96.jpg","videoPlayList":[]},{"id":"c10c38c0-832d-405c-b623-c387aebc964b","categoryId":"83e62001-3ddd-4ee1-85ab-d638896931d9","attachmentId":"550fb6f3-b6f9-4ce0-be52-a7962c22bd78","title":"仙逆","score":"8.1","alias":"Renegade Immortal","director":"石头熊,冯毅","actors":"","region":"中国","language":"汉语普通话","description":"改编自耳根同名小说《仙逆》，讲述了乡村平凡少年王林以心中之感动，逆仙而修，求的不仅是长生，更多的是摆脱那背后的蝼蚁之身。他坚信道在人为，以平庸的资质踏入修真仙途，历经坎坷风雨，凭着其聪睿的心智，一步一...","tags":"","author":"","coverUrl":"https://pic.youkupic.com/upload/vod/20230925-1/02513a0cae2b30d73eeb073f15ae6d56.jpg","videoPlayList":[]},{"id":"ecb81d9a-0512-4d67-9752-d4b2009f87fb","categoryId":"8886c5eb-1ec2-43ee-84f7-35217940992b","attachmentId":"6a924c67-46ae-4cb7-adba-0dc051b3670b","title":"沙尘暴","score":"8.1","alias":"","director":"韩琰","actors":"王安宇 / 王玉雯 / 黄子琪 / 赵佳丽 / 张帆 / 杨昀昊 / 耿乐 / 张宥浩 / 孙嘉灵 / 程莉莎 / 郭晓东","region":"中国","language":"国语","description":"一桩焚尸旧案被重查，基层民警陈江河（段奕宏 饰）也被紧急调回。当他再次进入小城，搅局的骗子、撒谎的真凶、复仇的牺牲者，犹如一场沙尘暴将他笼罩其中。但他坚信罪恶皆有源，再大的风暴也终会平息，天地总会恢复...","tags":"","author":"","coverUrl":"https://pic.youkupic.com/upload/vod/20250402-1/ef17d0b961fd087bc5b182e96c28edef.jpg","videoPlayList":[]},{"id":"0a4eb8dc-8f70-4775-b50c-4e4672e31363","categoryId":"8886c5eb-1ec2-43ee-84f7-35217940992b","attachmentId":"3717bab2-a982-4012-8440-29fe687d67bb","title":"念无双","score":"0.0","alias":"天下无双,念无双 剧版","director":"郭虎","actors":"唐嫣,刘学义,郭晓婷,王弘毅,周柯宇,张俪,陈楚河,郑希怡,白澍,郑业成,马启越,郑国霖,修庆,丁嘉文,李泊文,曾淇,傅淼,杨肸子,朱戬","region":"中国","language":"汉语普通话","description":"数万年前，神魔大战，源生之神泰和利用世间至高至强的神器“神之左手”封印魔神，结果神器折毁坠落人界，源生之众神陷人长眠，自此三界再无神迹。战鬼族趁乱崛起，引发大战，神女无双受天界之托，化身人族少女进入神...","tags":"","author":"赵娜,十四郎","coverUrl":"https://pic.youkupic.com/upload/vod/20240809-1/3ca4b79fff52126b4223f26251edbd93.jpg","videoPlayList":[]},{"id":"a0da2928-2796-4aa2-9aab-9684d2837b98","categoryId":"8886c5eb-1ec2-43ee-84f7-35217940992b","attachmentId":"f2192361-7af3-4ce7-87ed-8ff6b02a7c43","title":"黄雀","score":"7.1","alias":"","director":"卢伦常","actors":"郭京飞,秦岚,祖峰,陈靖可,郭柯宇,赵滨,王浩信,吕晓霖,张皓然,马吟吟,王铮,路宏,郭丞,周政杰,姜大卫,尤勇智,何蓝逗,刘頔,房子斌","region":"中国","language":"汉语普通话","description":"2004年，郭鹏飞调到荔城参与反扒专项整治，同时寻找失踪的未婚妻方慧。大鹏与反扒新人李唐成为搭档，追踪眼角膜案背后的盗窃团伙，过程中结识了医务室大夫黎小莲。经过一番调查，反扒大队锁定盗窃团伙首脑佛爷，...","tags":"","author":"王小枪","coverUrl":"https://pic.youkupic.com/upload/vod/20250318-1/a6f2007e4f08bb64f4626b61952277fb.jpg","videoPlayList":[]},{"id":"596a782e-cac2-4774-919f-d0279ccbe4a9","categoryId":"7009a342-9fb1-4020-8abe-1eef79400e4e","attachmentId":"646196ea-9cf7-4997-86f8-1dcd9ca642b5","title":"射雕英雄传：侠之大者","score":"5.2","alias":"射雕三部曲1,The Legend of the Condor Heroes: The Great Hero","director":"徐克","actors":"肖战,庄达菲,梁家辉,张文昕,巴雅尔图,阿如那,蔡少芬,胡军,吴兴国,纳仁巴特尔,依特格勒,李海涛,图门巴雅尔,威力斯,元彬,杜玉明,释彦能,许明虎,李晨,李欣阳,孙建魁,徐向东,王涌澄,刘占领 ","region":"中国","language":"汉语普通话,蒙语","description":"恩怨情仇的江湖，权势角力的战乱时代，郭靖（肖战 饰）童年离别家乡，逐渐炼就可改变局面和命运的庞大力量。虽受高人赏识和器重，得传天下绝世武功“九阴真经”和“降龙十八掌”，却惹来各方嫉忌，成为众矢之的。\n...","tags":"","author":"徐克,宋譞","coverUrl":"https://pic.youkupic.com/upload/vod/20250131-1/4da3d2e92dabe6a6f404c1f655d47aff.jpg","videoPlayList":[]},{"id":"ae12ba35-bdad-48f8-8692-ba8b2fab95f4","categoryId":"83e62001-3ddd-4ee1-85ab-d638896931d9","attachmentId":"f0affabb-f812-43b7-b178-4d1029724020","title":"武动乾坤 第五季","score":"6.3","alias":"Martial Universe Ⅴ","director":"","actors":"萧清源 KIYO","region":"中国","language":"汉语普通话","description":"少年林动为寻吞噬祖符下落，深入大荒古碑，不断潜心修炼，终于迎来族比之战… 面对将父亲打成废人、对自己痛下狠手的对手林氏天才林琅天，林动心中屈辱难平，亟待雪耻逆袭！","tags":"","author":"","coverUrl":"https://pic.youkupic.com/upload/vod/20250101-1/7f12a42d0457540eacad9ded93512108.jpg","videoPlayList":[]},{"id":"29d60db9-97ff-4827-bc87-bfb27d8c84b8","categoryId":"016ee3b9-fa1e-4543-9d95-990e5a3ab375","attachmentId":"7a3b171d-6e7d-46c2-abe6-20967e737f09","title":"误杀3","score":"6.2","alias":"Octopus with Broken Arms","director":"甘剑宇","actors":"肖央,佟丽娅,段奕宏,刘雅瑟,王龙正,冯兵,周楚濋,徐诣帆,高捷,尹子维,张榕容,卢慧敏,陈昊,范静祎,叶泉希","region":"中国,香港","language":"汉语普通话","description":"郑炳睿（肖央 饰）的宝贝女儿婷婷众目睽睽之下，遭神秘绑匪绑架，始终陪伴父女左右的李慧萍（佟丽娅 饰）与他一同展开救女行动。但狡诈绑匪轻松躲避警方负责人张景贤（段奕宏 饰）的密集追捕，更将救女心切的郑、...","tags":"","author":"陈思诚,武皮皮,李鹏,胡小楠","coverUrl":"https://pic.youkupic.com/upload/vod/20250101-1/b428803c57aef9bd26be7f47b5093620.jpg","videoPlayList":[]},{"id":"1ab35e23-677d-4c0b-80ff-f5256103a94d","categoryId":"83e62001-3ddd-4ee1-85ab-d638896931d9","attachmentId":"5b0ce4bc-ee5e-4703-ad8b-4d1182066bbd","title":"大道朝天","score":"8.2","alias":"大道朝天 动画版,The Path Toward Heaven","director":"陈升垚","actors":"羊仔,阎么么,李璐,锦鲤,王凯,杨默,王晨光,阎萌萌,齐斯伽,苗壮,王敏纳,张博恒,孟宇,赵铭洲,赵毅,李诗萌,赵梦娇,魏超,林强,苏雨山,褚珺,路熙然,张磊,宇薇,云惟一,孙路路","region":"中国","language":"汉语普通话","description":"踏遍青山人未老，蓦然回首，那剑才下眉间，却在心间。\n朝天大陆内避居山村的井九，意外收得天生道种的柳十岁为书童。两人凭借机缘来到青山宗，却因为“平平无奇“的井九居然以“天赋卓绝”的天才柳十岁作为仆人，遭...","tags":"","author":"王旭阳","coverUrl":"https://pic.youkupic.com/upload/vod/20241010-1/afcb815cba2828ef3acfdf829dbaed25.jpg","videoPlayList":[]},{"id":"f837136c-b98d-497d-b581-c475ff240189","categoryId":"83e62001-3ddd-4ee1-85ab-d638896931d9","attachmentId":"ece3f98f-2ebe-4216-be10-ef59f4dc1e3f","title":"一念永恒 第三季","score":"0.0","alias":"A Will Eternal","director":"苏晓光","actors":"苏尚卿","region":"中国","language":"汉语普通话","description":"白小纯合并下游四宗成立逆河宗后，成功挑战中游三宗立稳脚跟，被上游星空道极宗天人指名做质子，开启第三季质子篇。","tags":"","author":"","coverUrl":"https://kuaichezyimg.com/upload/vod/20250506-1/255e24c73bf1b08898c4ede094266286.jpg","videoPlayList":[]},{"id":"99ee71b9-dd2b-4486-9971-15ccfb054933","categoryId":"a8397aa0-1db9-4ff8-b6f4-0e4a600c6142","attachmentId":"53cd716a-1fe6-4ff6-b613-5c4b99871a19","title":"阿诺拉 Anora","score":"6.4","alias":"艾诺拉(台)","director":"肖恩·贝克","actors":"米奇·麦迪森,尤拉·鲍里索夫,马克·埃德尔斯坦,艾薇·沃尔克,Lindsey Normington,达里娅·叶卡马索娃,阿列克谢·谢列布里亚科夫,卡伦·卡拉古利安,Emily Weider,Alena Gurevich,Luna Sofía Miranda,罗斯·布罗达尔,Vache Tovmasyan,Charlton Lamar,Masha Zhak,Paul Weissman,Sophia Carnabuci","region":"美国","language":"英语,俄语,亚美尼亚","description":"在布鲁克林工作的阿诺拉邂逅俄罗斯商界寡头之子伊万，冲动中嫁给了他，幻想自己成了灰姑娘。然而，伊万的父母得知后立刻赶到纽约，强行逼迫两人离婚。在一系列风波后，阿诺拉的美梦最终破灭。","tags":"","author":"肖恩·贝克","coverUrl":"https://pic.youkupic.com/upload/vod/20241218-1/bb1c146189d998f7e605a6686db308dc.jpg","videoPlayList":[]},{"id":"a960a7e8-d4e0-4043-b7f3-956f9ec39f60","categoryId":"7009a342-9fb1-4020-8abe-1eef79400e4e","attachmentId":"efbbde23-daca-4f76-a58c-d10a211a21e3","title":"沙丘2 Dune: Part Two","score":"8.1","alias":"沙丘：第二部(台),沙丘瀚战：第二章(港),沙丘II,Dune 2","director":"丹尼斯·维伦纽瓦","actors":"提莫西·查拉梅,赞达亚,丽贝卡·弗格森,弗洛伦丝·皮尤,奥斯汀·巴特勒,蕾雅·赛杜,哈维尔·巴登,斯特兰·斯卡斯加德,乔什·布洛林,戴夫·巴蒂斯塔,克里斯托弗·沃肯,蒂姆·布雷克·尼尔森,夏洛特·兰普林,安雅·泰勒-乔伊,斯蒂芬·亨德森,安东·桑德斯,索海拉·雅各布,特雷茜库根,阿伦·梅迪扎德,伊莫拉·加斯帕尔,塔拉·布雷思纳克,小彼得·斯托亚诺夫,莫利·麦考恩","region":"美国,加拿大","language":"英语","description":"《沙丘2》承接第一部剧情，讲述保罗·厄崔迪（提莫西·查拉梅 Timothée Chalamet 饰）被帕迪沙皇帝和哈克南人联手灭族后，在厄拉科斯星球遇到弗雷曼女战士契妮（赞达亚 Zendaya 饰）以...","tags":"","author":"乔·斯派茨,丹尼斯·维伦纽瓦,弗兰克·赫伯特","coverUrl":"https://pic.youkupic.com/upload/vod/20240227-1/41f8c0b80210444000568428455e4b60.jpg","videoPlayList":[]},{"id":"3cc0e2f8-1fca-4afe-98f2-288767c3a607","categoryId":"83e62001-3ddd-4ee1-85ab-d638896931d9","attachmentId":"a2d67234-2495-4391-bbb8-5f108a642fb3","title":"遮天 年番1","score":"6.2","alias":"遮天 动画版,Shrouding the Heavens","director":"","actors":"","region":"中国","language":"汉语普通话","description":"本作动画改编自起点白金作者辰东遮天三部曲的第一部——遮天。冰冷与黑暗并存的宇宙深处，九具庞大的龙尸拉着一口青铜古棺，亘古长存。这是太空探测器在枯寂的宇宙中捕捉到的一幅极其震撼的画面。九龙拉棺，究竟是回...","tags":"","author":"","coverUrl":"https://kuaichezyimg.com/upload/vod/20250506-1/b5093f050128e9b53ade98239fc4e3c9.jpg","videoPlayList":[]},{"id":"8414796b-8b9c-4029-9c47-0e5c1a9df1fa","categoryId":"8886c5eb-1ec2-43ee-84f7-35217940992b","attachmentId":"1eb8030d-3f03-4328-aeab-9dd7a38ec220","title":"棋士","score":"0.0","alias":"Playing Go","director":"房远,高妮妮","actors":"王宝强,陈明昊,陈永胜,王智,李乃文,李梦,邢佳栋,李易祥,吴天琪,李若天,刘禹泽,陈卫,张宁浩,董向荣,赵毅,鲍振江","region":"中国","language":"国语","description":"《棋士》主要探讨了世纪初剧烈社会变革下⼈的状态和选择。以世纪初的南方城市为背景，讲述了一个普通的围棋老师崔业（王宝强 饰）因一场意外卷入罪案，逐步走向犯罪，被身为警察的哥哥崔伟（陈明昊 饰）穷追不舍，","tags":"","author":"","coverUrl":"https://pic.youkupic.com/upload/vod/20250325-1/60b56e56f9d00a639454214ce6063ba6.jpg","videoPlayList":[]},{"id":"e41ac81f-6da8-490c-ade9-2ed8b946ec4e","categoryId":"0ba01ca6-2dd8-41fb-b1a4-64f59ae627de","attachmentId":"ccb8ac31-7658-4db4-9030-59121a7402b4","title":"画江湖之不良人7","score":"0.0","alias":"画江湖之不良人7/画江湖之不良人Ⅶ,画江湖之不良人 第七季,画江湖之不良人柒：九幽玄天","director":"唐媛,付亮","actors":"边江,申秋香","region":"中国","language":"汉语普通话","description":"漠北当下表面平静，实则暗中涌动。王后述里朵正筹划一场大会，不良帅为探查漠北动向带着一众不良人出现在漠北，以降臣为首的四大尸祖在此期间也来到漠北开了家古董羹店，而神秘的杀人凶手更弄得人心惶惶，几方势力各...","tags":"","author":"","coverUrl":"https://pic.youkupic.com/upload/vod/20250327-1/a68ba13fb21f0e590eca7d0bc9d83773.jpg","videoPlayList":[]},{"id":"f4bce862-7a4c-4ec0-aefa-6357ecce6515","categoryId":"492e79cc-885e-4ac9-9bce-ad58ee3e2102","attachmentId":"f182b50a-4d1a-4008-a1c3-6ef7ab91106e","title":"苦尽柑来遇见你 폭싹 속았수다","score":"9.5","alias":"辛苦了,您辛苦了,你辛苦了,When Life Gives You Tangerines,You Have Done Well","director":"金元锡","actors":"李知恩,朴宝剑,文素利,朴解浚,金容琳,罗文姬,廉惠兰,吴敏爱,崔代勋,张慧珍,白智媛,嵯美暻,郑熙俊,安津罗森,吴正世,严志媛,金宣虎,李濬荣,郑伊书,金太延,李天茂,柳炳勋,李秀卿,李秀美","region":"韩国","language":"韩语","description":"该剧讲述叛逆勇敢的爱纯（李知恩 饰）与坚定不移的宽植（朴宝剑 饰）在济州岛花开花落四季中的动人故事。","tags":"","author":"林尚春","coverUrl":"https://pic.youkupic.com/upload/vod/20250307-1/6776996636c911eaca6cbdf8e5ec28bd.jpg","videoPlayList":[]},{"id":"3f058389-d9b3-424e-ba6b-9a479e365640","categoryId":"8886c5eb-1ec2-43ee-84f7-35217940992b","attachmentId":"8d8039c2-5fe0-4694-b6ab-681274286d05","title":"北上","score":"7.1","alias":"Northward","director":"姚晓峰,周楠","actors":"白鹿,欧豪,翟子路,高至霆,李宛妲,刘恒甫,胡军,李乃文,岳旸,齐欢,刘威葳,童蕾,涂凌,刘敏,朱铁,褚栓忠,王学圻,萨日娜","region":"中国","language":"汉语普通话","description":"故事讲述了运河边长大的年轻人从花街到北京，在度过了他们青涩纯真的青春时光，也经历了积极而艰难的北上创业时期，当荣耀或失败都已成为过往之际，他们再次重归运河边的花街，寻找真实自我和人生意义的故事。\n该剧...","tags":"","author":"赵冬苓","coverUrl":"https://pic.youkupic.com/upload/vod/20250303-1/5e05f05e591bf4b7e0202ca618b3fe55.jpg","videoPlayList":[]},{"id":"d96b66aa-e985-42c0-a253-6bd549b8440d","categoryId":"8886c5eb-1ec2-43ee-84f7-35217940992b","attachmentId":"d108c34a-292d-4ce3-bcf2-8a5082b9afb8","title":"似锦","score":"5.7","alias":"Sijin","director":"王为,李才","actors":"景甜,张晚意,郭涛,张弛,徐好,白冰可,马苏,黄奕,章呈赫,吴冕,戴春荣,曹斐然,崔航,赵昕,岳旸,宣言,管栎,刘亭作,嘉泽,天爱,夏娃,钟丽丽,大黄,钢镚","region":"中国","language":"汉语普通话","description":"讲述东平伯府四姑娘姜似（景甜 饰）死在爱人郁锦（张晚意 饰）手中后，重启命运的故事。这一次，姜似勇敢退婚、智斗恶毒婶婶、开设香铺、助好友护家人……一点点将悲剧改写。姜似与郁锦再度相遇，二人虽各怀心事，...","tags":"","author":"李文婷,蔡蔡,刘佳思,王珂,董芳辰,王翔","coverUrl":"https://pic.youkupic.com/upload/vod/20250301-1/65a7088ea8c32477d3e21a29f30db0fa.jpg","videoPlayList":[]},{"id":"95ef9201-b7f7-492e-9b36-b4d6bbb4c1ab","categoryId":"83e62001-3ddd-4ee1-85ab-d638896931d9","attachmentId":"9c8f120f-ca3e-4b5c-a4f4-569db47fa804","title":"星辰变  第六季","score":"0.0","alias":"星辰变之神界篇,Stellar Transformation Ⅵ","director":"汪成果","actors":"沈达威","region":"中国","language":"汉语普通话","description":"秦羽在逐步了解神界的规则后，为了迎娶属于神界八大家族与自己身份悬殊的姜立，选择立志成为最强匠神，随后从剿灭山贼开始，成就上级神人，被圣皇看重成为新贵……一步一步最终达到了可以光明正大向立儿求亲的身份。...","tags":"","author":"","coverUrl":"https://pic.youkupic.com/upload/vod/20250129-1/f4edf705728860122bcf931e8ac9ed4b.jpg","videoPlayList":[]},{"id":"371471e7-8a1b-4101-97de-53e8d235a3e3","categoryId":"8886c5eb-1ec2-43ee-84f7-35217940992b","attachmentId":"a63b268d-839c-4216-86df-d4e169ffa688","title":"五福临门","score":"6.0","alias":"","director":"杨欢,白云默,马诗歌","actors":"倪虹洁,卢昱晓,王星越,梁永棋,柯颖,吴宣仪,黄圣池,董思成,黄杨钿甜,刘些宁,陈鹤一,赵晴,曾舜晞,蓝盈莹,孙晶晶,李昀锐,左小青,董春辉,程莉莎,白川,李明德,昌隆","region":"中国","language":"汉语普通话","description":"北宋仁宗年间，汴京物阜民丰，声名远播天下。洛阳富户郦娘子举家远迁汴京，一为投奔早嫁的二女福慧，二为解决一桩陈年心事。\n郦娘子一生最得意的事便是有五位如花似玉的千金，可惜大娘寿华青春守寡、无心再嫁；三娘...","tags":"","author":"周末","coverUrl":"https://pic.youkupic.com/upload/vod/20250125-1/bda8728ea5e5d4c6205fda033378e938.jpg","videoPlayList":[]}]}
    """.trimIndent()
    
    // 使用Moshi解析JSON数据（实际项目中应该使用ViewModel和Repository）
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    val jsonAdapter = moshi.adapter(VideoListResponse::class.java)
    val videoListResponse = remember { jsonAdapter.fromJson(videoListJson) ?: VideoListResponse() }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(videoListResponse.data) { video ->
            VideoCard(video = video, onClick = { onVideoClick(video) })
        }
    }
}

@Composable
fun VideoCard(video: VideoItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column {
            AsyncImage(
                model = video.coverUrl,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "导演: ${video.director}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = "评分: ${video.score}",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}