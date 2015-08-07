package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by Chrome on 5/9/15.
 *
 * 테이블을 리셋하고 프리셋을 생성하는 클래스
 */
public final class ActionPreset {
    private static ActionPreset ourInstance = new ActionPreset();
    public static ActionPreset getInstance() {
        return ourInstance;
    }

    ActionMain actionMain;
    LockWrapper.ReadLockWrapper read_lock;
    LockWrapper.WriteLockWrapper write_lock;

    private ActionPreset() {
        actionMain = ActionMain.getInstance();
        read_lock = actionMain.getReadLock();
        write_lock = actionMain.getWriteLock();
    }

    public void revert_to_default(Context context) {
        write_lock.lock();
        delete_flag();
        actionMain.resetTables();
        ExternalImageProcessor.remove_all_images(context);
        mark_flag();
        recover_preset();
        write_lock.unlock();
    }

    public void recover_preset() {
        write_lock.lock();
        ActionMacro actionMacro = (ActionMacro)actionMain.itemChain[ActionMain.item.ID_Macro];
        ActionGroup actionGroup = (ActionGroup)actionMain.itemChain[ActionMain.item.ID_Group];

        long id;

        // TODO: 이후 프리셋이 정규로 편입될 때 레퍼 클래스를 만들어서 대량으로 한꺼번에 삽입하게 만들기.
        id = actionGroup.add(1, 0, "테스트", R.drawable.color);
        actionMacro.add(id, 0, "나는 당신을 사랑합니다", R.drawable.bookmark);

////        actionMacro.add(id, 0, "인터넷에 도는 유명인들의 어록의 단점은 진위를 알기 어렵다는 점이다.", R.drawable.expression);


        long quotes_id = actionGroup.add(1, 0, "격언", R.drawable.bookmark);
        id = actionGroup.add(quotes_id, 0, "ㄱ", R.drawable.bookmark);
        actionMacro.add(id, 0, "가난은 가장 나쁜 종류의 폭력이다.", R.drawable.expression);
        actionMacro.add(id, 0, "가능의 한계를 알기 위한 유일한 방법은 불가능의 영역에 살짝 발을 들여 놓아 보는 것.", R.drawable.expression);
        actionMacro.add(id, 0, "가장 나쁜 삶은 가난한 삶이 아니라 노예의 삶이다.", R.drawable.expression);
        actionMacro.add(id, 0, "가장 큰 약점은 약점을 보일 것에 대한 두려움이다.", R.drawable.expression);
        actionMacro.add(id, 0, "감사하는 마음은 개나 앓는 질병이다.", R.drawable.expression);
        actionMacro.add(id, 0, "갓 태어난 아기가 무슨 쓸모가 있겠습니까?", R.drawable.expression);
        actionMacro.add(id, 0, "개가 아무리 유창하게 짖어도 제 부모가 가난했지만 정직했었노라고 말할 수 없다.", R.drawable.expression);
        actionMacro.add(id, 0, "개에게 물린 상처는 개를 죽인다고 아물지 않는다.", R.drawable.expression);
        actionMacro.add(id, 0, "개 한 마리를 훔치면 불인(不仁)이라고 한다. 그런데도 한 나라를 훔치고 이를 의(義)라고 한다.", R.drawable.expression);
        actionMacro.add(id, 0, "개가 날 보고 짖는다고 그 개를 죽이진 않겠소.", R.drawable.expression);
        actionMacro.add(id, 0, "거만한 사과란 모욕이나 다름없다.", R.drawable.expression);
        actionMacro.add(id, 0, "거짓과 더불어 제정신으로 사느니, 진실과 더불어 미치는 쪽을 택하고 싶다.", R.drawable.expression);
        actionMacro.add(id, 0, "거짓말은 처음에는 부정되고, 그 다음에는 의심받지만, 되풀이하면 결국 모든 사람이 믿게 된다.", R.drawable.expression);
        actionMacro.add(id, 0, "결국 죽음이란 나쁜 것을 잃는 것이다.", R.drawable.expression);
        actionMacro.add(id, 0, "고고학자는 여성에게 있어서 최고의 남편감이다. 여자가 늙으면 늙을수록 남편이 더 흥미를 가질 것이기 때문이다.", R.drawable.expression);
        actionMacro.add(id, 0, "공장다운 공장을 지으려면 적어도 20만 평은 되어야 하지 않겠는가?", R.drawable.expression);
        actionMacro.add(id, 0, "공포는 사람을 겸손하게 만든다.", R.drawable.expression);
        actionMacro.add(id, 0, "공포 앞에 논쟁이란 없다.", R.drawable.expression);
        actionMacro.add(id, 0, "교통체증 때문에 가뜩이나 서민들이 어려움을 겪으니 내 장례식은 절대 치르지 마라!", R.drawable.expression);
        actionMacro.add(id, 0, "과거를 기억 못하는 이들은 과거를 반복하기 마련이다.", R.drawable.expression);
        actionMacro.add(id, 0, "과학에는 국경이 없다. 하지만 과학자에게는 조국이 있다.", R.drawable.expression);
        actionMacro.add(id, 0, "괴물과 싸우는 사람은 그 싸움 속에서 스스로도 괴물이 되지 않도록 조심해야 한다. 우리가 괴물의 심연을 오래동안 들여다 본다면, 그 심연 또한 우리를 들여다 보게될 것이다.", R.drawable.expression);
        actionMacro.add(id, 0, "괴짜들에게 잘해주어라.나중에 그들 밑에서 일할 날이 올지도 모른다.", R.drawable.expression);
        actionMacro.add(id, 0, "교황이라! 그자는 몇 개 사단을 가지고 있지?", R.drawable.expression);
        actionMacro.add(id, 0, "국민의, 국민에 의한, 국민을 위한 정부는 지구상에서 사라지지 않을 것이다.", R.drawable.expression);
        actionMacro.add(id, 0, "국민들에게 무조건 불쾌한 뉴스를 숨기는 것은 심각한 실수이다. 적당한 낙관주의를 기본 태도로 삼아야 하지만, 모든 부문에서 좀 더 현실적으로 변해야 한다. 국민들은 이를 능히 소화해낼 수 있고 또한 그래야만 한다.", R.drawable.expression);
        actionMacro.add(id, 0, "국민을 비굴하게 만드는 정치가 가장 나쁜 정치이다.", R.drawable.expression);
        actionMacro.add(id, 0, "군대는 배가 불러야 움직인다.", R.drawable.expression);
        actionMacro.add(id, 0, "군자가 소인을 다스림은 언제나 느슨하니, 까닭에 소인은 틈을 보아 다시 일어난다. 소인이 군자를 해침은 언제나 참혹하니, 까닭에 한 번 그물질에 남는 것이 없다. 쇠미한 세상에서는 소인을 제거하는 자 역시 소인이다. 한 소인이 물러가니 한 소인이 나아가는도다. 이기고 지는 자, 소인들뿐이로구나!", R.drawable.expression);
        actionMacro.add(id, 0, "굶주린 개를 주워 잘 돌보면 그 개는 절대 당신을 물지 않을 것이다. 이 점이 바로 인간과 개의 근본적인 차이점이다.", R.drawable.expression);
        actionMacro.add(id, 0, "권력은 총구에서 나온다.", R.drawable.expression);
        actionMacro.add(id, 0, "그간 우리에게 가장 큰 피해를 끼친 말은 바로 ‘지금껏 항상 그렇게 해왔어’라는 말이다.", R.drawable.expression);
        actionMacro.add(id, 0, "그게 내 일이었다. 마침 적절한 시간에 내가 할 일을 한 것, 그뿐이다.", R.drawable.expression);
        actionMacro.add(id, 0, "그대가 읽을 수만 있다면 영원히 자유가 되리라.", R.drawable.expression);
        actionMacro.add(id, 0, "그러니 우리 모두 자신을 자신에게 주어진 역할로 무장하고, 그렇게 견디어 내어, 대영제국과 영연방이 수천 년이 지나도 사람들이 여전히 이 때가 그들의 가장 영광스러운 시간이었다고 회자할 수 있도록 합시다.", R.drawable.expression);
        actionMacro.add(id, 0, "근로기준법을 준수하라! 우리는 기계가 아니다! 일요일은 쉬게 하라! 노동자들을 혹사하지 말라! 내 죽음을 헛되이 하지 말라!", R.drawable.expression);
        actionMacro.add(id, 0, "근거는 없지만 전방의 소대장이 올바르고, 후방의 사령관이 틀렸다.", R.drawable.expression);
        actionMacro.add(id, 0, "끝날 때까지는 끝난 게 아니다.", R.drawable.expression);
        actionMacro.add(id, 0, "기르기 시작한 이상 잡초가 아닙니다.", R.drawable.expression);
        actionMacro.add(id, 0, "기억은 생명체와 같다.", R.drawable.expression);
        actionMacro.add(id, 0, "기업이 발전하려면 주변의 모든 기업이 함께 발전해야 하고, 또한 경쟁자가 있어야 한다.", R.drawable.expression);

        id = actionGroup.add(quotes_id, 0, "ㄴ", R.drawable.bookmark);
        actionMacro.add(id, 0, "나(지식인)는 개와 같아서, 사람들이 깨든 안 깨든 도둑을 보고 밤새 짖었다.", R.drawable.expression);
        actionMacro.add(id, 0, "나는 길을 찾든가 아니면 만들어 낼 것이라.", R.drawable.expression);
        actionMacro.add(id, 0, "나는 당신의 말에 동의하지 않는다. 하지만 당신이 그 말을 할 권리를 나는 죽음을 불사하여 사수하겠다.", R.drawable.expression);
        actionMacro.add(id, 0, "나는 죽어가지만 항복은 하지 않는다! 조국이여, 안녕.", R.drawable.expression);
        actionMacro.add(id, 0, "나는 생각한다. 고로 나는 존재한다.", R.drawable.expression);
        actionMacro.add(id, 0, "나는 아직도 배가 고프다.", R.drawable.expression);
        actionMacro.add(id, 0, "나는 어떠한 아메리칸 드림도 보지 못하고, 아메리칸 악몽(나이트메어)을 봅니다.", R.drawable.expression);
        actionMacro.add(id, 0, "나는 예수 그리스도를 진심으로 사랑하고 존경하지만, 그의 신도를 자처하는 사람들은 모두 예수의 가르침과 반대로 말하고 행동한다.", R.drawable.expression);
        actionMacro.add(id, 0, "나는 자신의 창조물을 포상하고 징벌한다든지, 우리와 이해할 수 있는 의지를 지닌다는 신을 상상할 수조차 없다.", R.drawable.expression);
        actionMacro.add(id, 0, "나는 적에 맞서 백성을 편안케 한다는 말은 들었어도, 백성을 움직여 적을 피한다는 말은 듣지 못했다.", R.drawable.expression);
        actionMacro.add(id, 0, "나는 군사적 명성이라는 것에 대해 이해하고 있는것 같아. 그건 네가 전투에서 뒈져서 네 이름이 신문에 잘못 적히는 거지.", R.drawable.expression);
        actionMacro.add(id, 0, "나는 평화로운 노예로 사느니, 차라리 위험천만한 자유를 택하겠다.", R.drawable.expression);
        actionMacro.add(id, 0, "나는 천체의 운동을 계산할 수 있지만 사람들의 광기를 계산하지는 못한다.", R.drawable.expression);
        actionMacro.add(id, 0, "나로 말하자면, 나는 내가 아무것도 모른다는 것만을 알 뿐이네.", R.drawable.expression);
        actionMacro.add(id, 0, "나를 따라오라, 내가 너희로 사람을 낚는 어부가 되게 하리라.", R.drawable.expression);
        actionMacro.add(id, 0, "나를 죽일 수는 있어도 정의를 죽일 수는 없다.", R.drawable.expression);
        actionMacro.add(id, 0, "나에게 나무를 자를 여섯 시간을 준다면 나는 먼저 네 시간을 도끼를 날카롭게 하는 데 쓰겠다.", R.drawable.expression);
        actionMacro.add(id, 0, "나에게 노인이란 언제나 나보다 15살 많은 사람이다.", R.drawable.expression);
        actionMacro.add(id, 0, "나에게 영웅을 보여라, 비극을 써줄테니.", R.drawable.expression);
        actionMacro.add(id, 0, "나에게 한 문장만 달라. 누구든 범죄자로 만들 수 있다.", R.drawable.expression);
        actionMacro.add(id, 0, "내가 멀리 볼수 있었던 것은 오직 거인의 어깨위에 있었기 때문이다.", R.drawable.expression);
        actionMacro.add(id, 0, "내가 가난한 사람들에게 먹을 것을 나눠주면 그들은 나를 성자라고 부른다. 그러나 내가 사람들이 가난한 이유를 물으면 그들은 나를 공산주의자라고 부른다.", R.drawable.expression);
        actionMacro.add(id, 0, "내가 만약 남의 꾸짖음을 당하더라도 거짓으로 귀먹은 체하고 말을 분간하지 말라. 비유하건대 불이 허공에서 타다가 불끄지 않더라도 저절로 꺼지는 것과 같느니라. 나의 마음은 허공과 같거늘 모두 너의 입술과 혀만 너불거릴 뿐이니라.", R.drawable.expression);
        actionMacro.add(id, 0, "내 비장의 무기는 아직 내 손 안에 있다. 그것은 바로 희망이다.", R.drawable.expression);
        actionMacro.add(id, 0, "내가 정치와 전쟁에 관해 배우는건 나의 아이들에게 수학이나 철학을 배울 자유를 남기기 위함이다.", R.drawable.expression);
        actionMacro.add(id, 0, "내가 천하를 등질지언정 천하가 나를 등지게는 하지 않겠소.", R.drawable.expression);
        actionMacro.add(id, 0, "내가 죽고 나면 사람들은 내 무덤에 쓰레기를 집어던지겠지만, 결국에는 역사의 바람이 그 쓰레기들을 사정없이 모두 쓸어낼 것이다.", R.drawable.expression);
        actionMacro.add(id, 0, "내가 우주의 비밀을 쫓고 있는데 백만달러를 쫓겠는가?", R.drawable.expression);
        actionMacro.add(id, 0, "내게 죽어야 할 의리는 없지만, 다만 국가에서 500년이나 선비를 길러왔는데, 나라가 망할 때에 국난을 당하여 죽는 사람이 하나도 없다는 것이 어찌 원통치 않겠는가?", R.drawable.expression);
        actionMacro.add(id, 0, "내 키를 땅에서부터 재면 누구보다 작아도, 하늘로 부터 재면 누구보다 크다.", R.drawable.expression);
        actionMacro.add(id, 0, "너 자신을 알라.", R.drawable.expression);
        actionMacro.add(id, 0, "너는 어찌하여 닭을 잡는데 소 잡는 칼을 썼느냐?", R.drawable.expression);
        actionMacro.add(id, 0, "너가 좋아하지 않는 사람들을 생각하는데는 단 1분도 허비하지 마라.", R.drawable.expression);
        actionMacro.add(id, 0, "널리 배우고, 의문이 있으면 곧 묻고, 삼가 깊이 생각하며, 밝게 분변하고, 곧 이를 독실히 행하라.", R.drawable.expression);
        actionMacro.add(id, 0, "네가 싫어하는 것을 남에게 베풀지 마라.", R.drawable.expression);
        actionMacro.add(id, 0, "노동자들에게는 혁명 외에 희망이 없다.", R.drawable.expression);
        actionMacro.add(id, 0, "만일 그것이 사실이라면 노동자들에게는 진정 희망이 없다.", R.drawable.expression);
        actionMacro.add(id, 0, "노병은 죽지 않는다. 다만 사라질 뿐이다.", R.drawable.expression);
        actionMacro.add(id, 0, "농사는 하늘이 지어주는 것이 아니라 인간의 지혜와 노력으로서 짓는 것이다.", R.drawable.expression);
        actionMacro.add(id, 0, "누군가 망상에 시달리면 정신 이상이라고 한다. 다수가 망상에 시달리면 종교라고 한다.", R.drawable.expression);
        actionMacro.add(id, 0, "누구나 빵을 먹을 수 있을 때까지, 아무도 케이크를 먹어서는 안 된다.", R.drawable.expression);
        actionMacro.add(id, 0, "누구든 진실과 지식의 분야에서 자신을 판사라 생각하는 자는 신들의 웃음소리에 의해 박살날 것이다.", R.drawable.expression);
        actionMacro.add(id, 0, "눈물을 흘리며 씨를 뿌리는 자는 기쁨으로 거두리로다.", R.drawable.expression);
        actionMacro.add(id, 0, "늙은이들이 전쟁을 선포한다. 그러나 싸워야 하고 죽어야 하는 것은 젊은이들이다.", R.drawable.expression);
        actionMacro.add(id, 0, "늦었다고 생각할 때가 진짜 너무 늦었다. 그러니 지금 당장 시작하라.", R.drawable.expression);

        id = actionGroup.add(quotes_id, 0, "ㄷ", R.drawable.bookmark);
        actionMacro.add(id, 0, "다른 벼슬은 구해도 좋으나, 목민관의 벼슬은 구해서는 안 된다.", R.drawable.expression);
        actionMacro.add(id, 0, "달리는 기차 위에 중립은 없다.", R.drawable.expression);
        actionMacro.add(id, 0, "답이 없는 문제도 있기 마련이다.", R.drawable.expression);
        actionMacro.add(id, 0, "당신들은 노동자들에게 가시 돋힌 왕관을 씌울 수 없습니다. 당신들은 인류를 황금의 십자가에 못 박을 수 없습니다.", R.drawable.expression);
        actionMacro.add(id, 0, "당신은 당신이 생각하는 대로 살아야 한다. 그러지 않으면 머지않아 당신은 사는 대로 생각하게 될 것이다.", R.drawable.expression);
        actionMacro.add(id, 0, "당신은 전쟁에 관심이 없을 수도 있다. 그러나 전쟁은 당신에 관심이 있다.", R.drawable.expression);
        actionMacro.add(id, 0, "당신들이 어떻게 생각하든, 역사는 우리 편이다. 당신들을 묻어버리겠다.", R.drawable.expression);
        actionMacro.add(id, 0, "당신들에겐 시계가 있지만 우리들에겐 시간이 있다.", R.drawable.expression);
        actionMacro.add(id, 0, "당신이 가진 생각이 딱 하나밖에 없다면, 그것만큼 위험한 것은 없다.", R.drawable.expression);
        actionMacro.add(id, 0, "대의명분이 있다면 테러리스트라 불릴 수 없다.", R.drawable.expression);
        actionMacro.add(id, 0, "대장부가 왜 우느냐? 옛 사람이 말하기를 ‘나를 쓰다듬으면 임금이요, 나를 학대하면 원수로다.’고 하였다. 지금 왕의 행함이 잔학하여 사람을 죽이니 백성의 원수다. 네가 그를 죽여라.", R.drawable.expression);
        actionMacro.add(id, 0, "대중들은 작은 거짓말보다 더 큰 거짓말에 쉽게 속는다.", R.drawable.expression);
        actionMacro.add(id, 0, "대중은 여자와 같아 자신을 지배해 줄 강력한 지도자가 나타나기를 기다린다.", R.drawable.expression);
        actionMacro.add(id, 0, "대체 이 일을 어떻게 끝낼 수 있을까 모르겠지만, 성추행 당한 내 어린 딸과 다른 어린이들을 위해서, 그리고 그런 성도착자들을 처벌하지 않고 놓아두고자 하는 그런 이들이 있는 한 난 절대로 침묵하지 않을 것이다. 내 인생의 마지막 날까지 나는 아동 성추행범들과 싸울 것이다. 만약 나의 네 살짜리 딸조차도 보호할 수 없다면, 차라리 죽는 게 나을 것이다.", R.drawable.expression);
        actionMacro.add(id, 0, "더 이상 내 다리는 달릴 수 없지만 나에게는 아직 두 팔이 남아 있다.", R.drawable.expression);
        actionMacro.add(id, 0, "더 이상 추가할 것이 없을 때가 아니라 더 이상 뺄 것이 없을 때, 완벽함이 성취된다.", R.drawable.expression);
        actionMacro.add(id, 0, "독재자에게는 폭력을 써서라도 맞서 싸워야 한다.", R.drawable.expression);
        actionMacro.add(id, 0, "두 여자를 화해시키는 것은 유럽을 통일하는 것보다 어렵다.", R.drawable.expression);

        id = actionGroup.add(quotes_id, 0, "ㄹ", R.drawable.bookmark);

        id = actionGroup.add(quotes_id, 0, "ㅁ", R.drawable.bookmark);
        actionMacro.add(id, 0, "마지막 순간은 우리의 것입니다. 이 고통은 우리의 승리입니다.", R.drawable.expression);
        actionMacro.add(id, 0, "만국의 노동자들이여, 단결하라!", R.drawable.expression);
        actionMacro.add(id, 0, "만약 어떤 여자의 결점이 알고 싶다면, 그 여자의 친구들에게 그 여자를 칭찬하라.", R.drawable.expression);
        actionMacro.add(id, 0, "만약 당신이 누군가의 인격을 시험해 보고 싶다면, 그에게 권력을 줘 보라.", R.drawable.expression);
        actionMacro.add(id, 0, "만화는 당의정(糖衣錠)이다. 가장 접하기 쉬운 정보습득의 매체로서 만화는 유용하다는 것이다.", R.drawable.expression);
        actionMacro.add(id, 0, "몇 달을 두고 준비된 것은 몇 분 사이에 보상을 받고, 몇 초 사이에 잘못된 일은 몇 년 동안 부정적인 영향을 미친다. 그러므로 장기적이고도 단기적으로 대응하는 것이 중요하다. 그리고 그것은 신속한 반응 속도와 세심한 진행을 연계시키는 시간 관리에 의해서만 가능하다.", R.drawable.expression);
        actionMacro.add(id, 0, "모든 죽은 세대들의 전통은 마치 꿈 속의 악마처럼, 살아 있는 세대들의 머리를 짓누른다.", R.drawable.expression);
        actionMacro.add(id, 0, "모든 국가는 그들이 가질 만한 정부를 갖는다.", R.drawable.expression);
        actionMacro.add(id, 0, "모든 세대는 스스로를 이전 세대보다 똑똑하고, 다음 세대보다 현명하다고 생각한다.", R.drawable.expression);
        actionMacro.add(id, 0, "모든 전제군주는 자유를 믿고 살았다. 그 자신만의 자유를.", R.drawable.expression);
        actionMacro.add(id, 0, "무고한 민간인이란 없다. 민간인이 모조리 사라져야만 싸움을 멈출 것이다.", R.drawable.expression);
        actionMacro.add(id, 0, "무기는 설사 백년 동안 쓸 일이 없다 해도, 단 하루도 갖추지 않을 수 없다.", R.drawable.expression);
        actionMacro.add(id, 0, "무릇 잘된 정치를 하려면 반드시 전대의 잘 다스려진 세상과 어지러운 세상이 역사에 남긴 자취를 보아야 할 것이다.", R.drawable.expression);
        actionMacro.add(id, 0, "무릇 정치란 전쟁을 하지 않고 이기는 것이 제일이요, 전쟁을 하기 직전까지 가서 이기는 것이 그 다음이요, 전쟁을 통해서 이기는 것은 가장 낮음이니라.", R.drawable.expression);
        actionMacro.add(id, 0, "물리학 외의 과학은 우표수집에 불과하다.", R.drawable.expression);
        actionMacro.add(id, 0, "미국이 60년대가 가기 전에 인간을 달로 보내고 지구로 무사귀환시키는 것에 전력을 다해야 한다고 생각합니다.", R.drawable.expression);
        actionMacro.add(id, 0, "미숙한 사람은 \"당신이 필요하기 때문에 당신을 사랑합니다.\"라고 말한다. 성숙한 사람은 \"당신을 사랑하기 때문에 당신이 필요합니다.\"라고 말한다.", R.drawable.expression);

        id = actionGroup.add(quotes_id, 0, "ㅂ", R.drawable.bookmark);
        actionMacro.add(id, 0, "바로 카인의 대답입니다.", R.drawable.expression);
        actionMacro.add(id, 0, "반성되지 않는 삶은 인간으로서 살 가치가 없다.", R.drawable.expression);
        actionMacro.add(id, 0, "방황하는 이들 모두가 길을 잃은 것은 아니다.", R.drawable.expression);
        actionMacro.add(id, 0, "배부른 돼지가 되느니 배고픈 철학자가 되겠다.", R.drawable.expression);
        actionMacro.add(id, 0, "백성은 일정한 마음이 없다. 그러므로 생각나면 오고 싫어지면 가버리는 것은 진실로 그렇기 때문이다.", R.drawable.expression);
        actionMacro.add(id, 0, "백성을 좀먹는 자는 공맹(孔孟, 공자와 맹자)이 살아 돌아오더라도 능히 용서할 수 없다.", R.drawable.expression);
        actionMacro.add(id, 0, "법관이 국민들로부터 의심을 받게 된다면 최대의 명예손상이 될 것이다.", R.drawable.expression);
        actionMacro.add(id, 0, "보라! 우리는 캄캄한 밤 중에 자유의 종을 난타하는 타수(打手)의 일익(一翼)임을 자랑한다.", R.drawable.expression);
        actionMacro.add(id, 0, "분노와 증오는 대중을 열광시키는 가장 강력한 힘이다.", R.drawable.expression);
        actionMacro.add(id, 0, "분명히 말하건대, 너희가 여기 있는 형제들 중에서 가장 낮은 자에게 해준 것이 나에게 해준 것이다.", R.drawable.expression);
        actionMacro.add(id, 0, "분열되어 서로 다투는 집안은 살아남을 수 없습니다.", R.drawable.expression);
        actionMacro.add(id, 0, "불의를 이해해야만 정의를 이해할수 있다.", R.drawable.expression);
        actionMacro.add(id, 0, "붉은 군대는 전진할 때보다 후퇴할 때 더 큰 용기가 필요하다.", R.drawable.expression);
        actionMacro.add(id, 0, "빛의 속도보다 빠르게 여행하는 것은 없지만, 나쁜 소식만은 예외다. 그 녀석만은 특별한 물리법칙을 따른다.", R.drawable.expression);
        actionMacro.add(id, 0, "빠르기는 바람과 같고, 느리기는 숲과 같으며, 공격할 때는 불과 같고, 움직이지 않기는 산과 같으며, 알기 어렵기는 그늘과 같고 움직임은 천둥과 같아야 한다.", R.drawable.expression);

        id = actionGroup.add(quotes_id, 0, "ㅅ", R.drawable.bookmark);
        actionMacro.add(id, 0, "사람들은 할 말이 없으면 욕을 한다.", R.drawable.expression);
        actionMacro.add(id, 0, "사람들이죠. 이 백신에 특허는 없습니다. 태양에 특허를 낼 수 있나요?", R.drawable.expression);
        actionMacro.add(id, 0, "사람은 책을 만들고 책은 사람을 만든다.", R.drawable.expression);
        actionMacro.add(id, 0, "사람이 있는 한 전쟁은 있을 것이다.", R.drawable.expression);
        actionMacro.add(id, 0, "사람 하나라도 부당하게 잡아 가두는 정부 밑에서, 정의로운 사람이 진정 있어야 할 곳은 감옥이다.", R.drawable.expression);
        actionMacro.add(id, 0, "사랑이 커지면 사랑을 요구하지 않는다. 사랑이 커지면 더욱 사랑하고 싶을 뿐.", R.drawable.expression);
        actionMacro.add(id, 0, "사변이 해결되지 않는 근본 원인은 일본인이 진정한 일본인으로서 행동을 하지 않기 때문이다. 약탈 폭행을 저지르면서 무슨 황군인가? 현지의 일반 민중을 괴롭히면서 성전이란 또 뭔가? 대륙에서 일본 관민이 이런 식으로 살면서 폐하의 마음에 합치한다고 생각하는건가?", R.drawable.expression);
        actionMacro.add(id, 0, "사제는 어리석은 행동을 하는 것보다 어리석은 말을 하는 것으로 더 큰 피해를 본다.", R.drawable.expression);
        actionMacro.add(id, 0, "살다 보니 결국 번쾌 따위와 같은 항렬이 되었구나!", R.drawable.expression);
        actionMacro.add(id, 0, "살아야 한다면 민중과 함께, 죽어야 한다면 민중을 위해.", R.drawable.expression);
        actionMacro.add(id, 0, "삶에 익숙하기에 삶을 사랑하는 것이 아니라, 사랑에 익숙하기에 삶을 사랑하는 것이다.", R.drawable.expression);
        actionMacro.add(id, 0, "상대를 알고 나를 알면 백번 싸워도 위태롭지 않으리라.", R.drawable.expression);
        actionMacro.add(id, 0, "상상력은 지식보다 중요하다. 지식은 제한되어 있지만 상상력은 온 세상을 아우르고, 진보를 촉진하며, 진화의 시발점이 되기 때문이다.", R.drawable.expression);
        actionMacro.add(id, 0, "상황이 사람을 만드는 게 아니라 상황은 단지 사람이 어떤 종류의 인간인지를 보여줄 뿐이다. 지금 우리의 혁명은 적들에 의해 죽어가고 있다. 적에게 죽느니 차라리 우리가 흘린 피에 익사하는 길을 택하겠다. 한 발자국도 물러서지 마라.", R.drawable.expression);
        actionMacro.add(id, 0, "새는 알을 까고 나온다. 알은 세계다. 태어나려는 자는 한 세계를 파괴해야만 한다.", R.drawable.expression);
        actionMacro.add(id, 0, "새 한 마리만 그려 넣으면 남은 여백 모두가 하늘이어라.", R.drawable.expression);
        actionMacro.add(id, 0, "석 자 길이의 칼을 들고 하늘에 맹세하니 산과 강의 빛이 변하고, 한번 휘둘러 쓸어 버리니 산과 강이 피로 물들여질 것이다.", R.drawable.expression);
        actionMacro.add(id, 0, "선거 승패가 결정되는 주요한 이유는 많은 사람들이 누가 좋아서 투표하기보다는 누가 싫어서 투표한다는 사실에 있다.", R.drawable.expression);
        actionMacro.add(id, 0, "성숙이란 어릴 때 놀이에 열중하던 진지함을 다시 발견하는 데 있다.", R.drawable.expression);
        actionMacro.add(id, 0, "성을 쌓는 자는 망하고 길을 닦는 자는 흥하리라.", R.drawable.expression);
        actionMacro.add(id, 0, "세간에 ‘천금을 가진 부잣집 자식이 길거리에서 죽는 법은 없다’라고 하는데 빈말이 아니다. 무릇 보통사람들은 자기보다 열 배 부자에 대해서는 헐뜯고, 백 배가 되면 두려워하고, 천 배가 되면 그 사람의 일을 해주고, 만 배가 되면 그의 노예가 된다. 이것이 사물의 이치다.", R.drawable.expression);
        actionMacro.add(id, 0, "세상이 널 버렸다고 생각하지마라. 세상은 널 가진 적이 없다.", R.drawable.expression);
        actionMacro.add(id, 0, "세상에 이런 평화 협정이 어디 있는가. 이것은 단지 20년간의 휴전 협정이 체결됐을 뿐이다.", R.drawable.expression);
        actionMacro.add(id, 0, "세상에는 세 가지의 감춰질 수 없는 것이 있다. 해와 달, 그리고 진실이다.", R.drawable.expression);
        actionMacro.add(id, 0, "수녀들이 나와서 앞에 설 것이고, 그 앞에는 또 신부들이 있을 것이고, 그리고 그 맨 앞에서 나를 보게 될 것이다. 그러니까 나를 밟고 신부들을 밟고 수녀들까지 밟아야 학생들과 만난다.", R.drawable.expression);
        actionMacro.add(id, 0, "수레의 굴대 빗장보다 키가 큰 남자는 모두 죽여라.", R.drawable.expression);
        actionMacro.add(id, 0, "태양 수천 개가 휘황찬란하게 하늘에서 일시에 폭발한다면, 이는 전능한 자의 광채와도 같으리... 나는 죽음의 신이요, 세상의 파괴자가 되었도다.", R.drawable.expression);
        actionMacro.add(id, 0, "술을 마시되 취하지 말고, 사랑을 하되 감정에 매몰되지 말고, 훔치되 부자의 것만 건드려라.", R.drawable.expression);
        actionMacro.add(id, 0, "스탈린은 내 친구요. 그는 죽었소. 그가 편안하게 쉬도록 놔둡시다.", R.drawable.expression);
        actionMacro.add(id, 0, "슬픔도 노여움도 없이 살아가는 자는 조국을 사랑하고 있지 않다.", R.drawable.expression);
        actionMacro.add(id, 0, "승리하는 군대는 이길 수 있는 상황을 만들고 이후에 전쟁을 한다. 패배하는 군대는 먼저 전쟁을 일으키고 이후에 승리를 구한다.", R.drawable.expression);
        actionMacro.add(id, 0, "시는 슬픔의 결정체, 정치는 불만의 결정체.", R.drawable.expression);
        actionMacro.add(id, 0, "시민의 불복종은 시민의 타고난 권리이다.", R.drawable.expression);
        actionMacro.add(id, 0, "신앙이란, 진실이 아닌 줄 알면서도 그걸 믿는 일이다.", R.drawable.expression);
        actionMacro.add(id, 0, "나에게 신문 없는 정부와 정부 없는 신문 둘중에 고르라면, 나는 한치의 망설임도 없이 정부 없는 신문을 택하겠다.", R.drawable.expression);
        actionMacro.add(id, 0, "신은 남성을 길들이기 위해 여성을 창조하였다.", R.drawable.expression);
        actionMacro.add(id, 0, "신은 듣건대 정자(程子)가 말하기를 ‘기한(祁寒)과 서우(暑雨)를 무릅쓰고 농부들이 농사지은 곡식을 내가 얻어먹고 있는데, 이처럼 한가하게 세월을 보내니 그야말로 이 천지간에 하나의 좀벌레라 하겠다.’ 하였다 합니다. 신처럼 쓸모없는 자가 천지 가운데 헛되이 살면서 한 가지도 하는 일 없이 가만히 앉아 농민이 생산한 곡식을 먹으니, 이미 좀벌레 중에서도 더욱 심한 경우라 하겠습니다.", R.drawable.expression);
        actionMacro.add(id, 0, "신은 모든 곳에 있을 수 없기에 어머니를 만들었다.", R.drawable.expression);
        actionMacro.add(id, 0, "신은 주사위를 던지지 않는다고 전적으로 확신한다.", R.drawable.expression);
        actionMacro.add(id, 0, "신이 주사위를 갖고 무엇을 하건 상관하지 마라.", R.drawable.expression);
        actionMacro.add(id, 0, "신이 주사위를 던지긴 하는데, 가끔 보이지 않는 곳에서 던져서 우리를 헷갈리게 만든다.", R.drawable.expression);
        actionMacro.add(id, 0, "신은 죽었다! 신은 죽은 채로 있다! 그리고 우리가 그를 죽였다.", R.drawable.expression);
        actionMacro.add(id, 0, "신은 TV 안에 있다.", R.drawable.expression);
        actionMacro.add(id, 0, "실전상황은 스타크래프트가 아닙니다.", R.drawable.expression);
        actionMacro.add(id, 0, "싸우다 죽기는 쉬워도 길을 빌려 주기는 어렵다.", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
//        actionMacro.add(id, 0, "인터넷에", R.drawable.expression);
        write_lock.unlock();
    }

    public static final class SQL implements BaseColumns {
        public static final String TABLE_NAME = "LocalPreset";
        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY" +
                        " )";
        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    private void delete_flag() {
        actionMain.getDB().execSQL(SQL.SQL_DELETE_ENTRIES);
    }

    // 프리셋 삽입 마크 여부 확인 - 존재시
    private void mark_flag() {
        read_lock.lock();
        SQLiteDatabase db = actionMain.getDB();

        // 디버그 자료형식 삽입 여부 확인 - 존재시 생성 생략, 없다면 생성 후 마킹
        db.execSQL(SQL.SQL_CREATE_ENTRIES);
        Cursor c = db.rawQuery("SELECT " + SQL._ID + " FROM " + SQL.TABLE_NAME + " WHERE " + SQL._ID + "=1", null);
        c.moveToFirst();
        if (c.getCount() > 0) {
            read_lock.unlock();
            return;
        }

        ContentValues record = new ContentValues();
        record.put(SQL._ID, 1);
        db.insert(SQL.TABLE_NAME, null, record);
        c.close();
        record.clear();
        read_lock.unlock();
    }

}
