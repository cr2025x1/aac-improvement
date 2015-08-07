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

//        actionMacro.add(id, 0, "나(지식인)는 개와 같아서, 사람들이 깨든 안 깨든 도둑을 보고 밤새 짖었다.", R.drawable.expression);
//        actionMacro.add(id, 0, "나는 길을 찾든가 아니면 만들어 낼 것이라.", R.drawable.expression);
//        actionMacro.add(id, 0, "나는 당신의 말에 동의하지 않는다. 하지만 당신이 그 말을 할 권리를 나는 죽음을 불사하여 사수하겠다.", R.drawable.expression);
//        actionMacro.add(id, 0, "나는 죽어가지만 항복은 하지 않는다! 조국이여, 안녕.", R.drawable.expression);
//        actionMacro.add(id, 0, "나는 생각한다. 고로 나는 존재한다.", R.drawable.expression);
//        actionMacro.add(id, 0, "나는 아직도 배가 고프다.", R.drawable.expression);
//        actionMacro.add(id, 0, "나는 어떠한 아메리칸 드림도 보지 못하고, 아메리칸 악몽(나이트메어)을 봅니다.", R.drawable.expression);
//        actionMacro.add(id, 0, "나는 예수 그리스도를 진심으로 사랑하고 존경하지만, 그의 신도를 자처하는 사람들은 모두 예수의 가르침과 반대로 말하고 행동한다.", R.drawable.expression);
//        actionMacro.add(id, 0, "나는 자신의 창조물을 포상하고 징벌한다든지, 우리와 이해할 수 있는 의지를 지닌다는 신을 상상할 수조차 없다.", R.drawable.expression);
//        actionMacro.add(id, 0, "나는 적에 맞서 백성을 편안케 한다는 말은 들었어도, 백성을 움직여 적을 피한다는 말은 듣지 못했다.", R.drawable.expression);
//        actionMacro.add(id, 0, "나는 군사적 명성이라는 것에 대해 이해하고 있는것 같아. 그건 네가 전투에서 뒈져서 네 이름이 신문에 잘못 적히는 거지.", R.drawable.expression);
//        actionMacro.add(id, 0, "나는 평화로운 노예로 사느니, 차라리 위험천만한 자유를 택하겠다.", R.drawable.expression);
//        actionMacro.add(id, 0, "나는 천체의 운동을 계산할 수 있지만 사람들의 광기를 계산하지는 못한다.", R.drawable.expression);
//        actionMacro.add(id, 0, "나로 말하자면, 나는 내가 아무것도 모른다는 것만을 알 뿐이네.", R.drawable.expression);
//        actionMacro.add(id, 0, "나를 따라오라, 내가 너희로 사람을 낚는 어부가 되게 하리라.", R.drawable.expression);
//        actionMacro.add(id, 0, "나를 죽일 수는 있어도 정의를 죽일 수는 없다.", R.drawable.expression);
//        actionMacro.add(id, 0, "나에게 나무를 자를 여섯 시간을 준다면 나는 먼저 네 시간을 도끼를 날카롭게 하는 데 쓰겠다.", R.drawable.expression);
//        actionMacro.add(id, 0, "나에게 노인이란 언제나 나보다 15살 많은 사람이다.", R.drawable.expression);
//        actionMacro.add(id, 0, "나에게 영웅을 보여라, 비극을 써줄테니.", R.drawable.expression);
//        actionMacro.add(id, 0, "나에게 한 문장만 달라. 누구든 범죄자로 만들 수 있다.", R.drawable.expression);
//        actionMacro.add(id, 0, "내가 멀리 볼수 있었던 것은 오직 거인의 어깨위에 있었기 때문이다.", R.drawable.expression);
//        actionMacro.add(id, 0, "내가 가난한 사람들에게 먹을 것을 나눠주면 그들은 나를 성자라고 부른다. 그러나 내가 사람들이 가난한 이유를 물으면 그들은 나를 공산주의자라고 부른다.", R.drawable.expression);
//        actionMacro.add(id, 0, "내가 만약 남의 꾸짖음을 당하더라도 거짓으로 귀먹은 체하고 말을 분간하지 말라. 비유하건대 불이 허공에서 타다가 불끄지 않더라도 저절로 꺼지는 것과 같느니라. 나의 마음은 허공과 같거늘 모두 너의 입술과 혀만 너불거릴 뿐이니라.", R.drawable.expression);
//        actionMacro.add(id, 0, "내 비장의 무기는 아직 내 손 안에 있다. 그것은 바로 희망이다.", R.drawable.expression);
//        actionMacro.add(id, 0, "내가 정치와 전쟁에 관해 배우는건 나의 아이들에게 수학이나 철학을 배울 자유를 남기기 위함이다.", R.drawable.expression);
//        actionMacro.add(id, 0, "내가 천하를 등질지언정 천하가 나를 등지게는 하지 않겠소.", R.drawable.expression);
//        actionMacro.add(id, 0, "내가 죽고 나면 사람들은 내 무덤에 쓰레기를 집어던지겠지만, 결국에는 역사의 바람이 그 쓰레기들을 사정없이 모두 쓸어낼 것이다.", R.drawable.expression);
//        actionMacro.add(id, 0, "내가 우주의 비밀을 쫓고 있는데 백만달러를 쫓겠는가?", R.drawable.expression);
//        actionMacro.add(id, 0, "내게 죽어야 할 의리는 없지만, 다만 국가에서 500년이나 선비를 길러왔는데, 나라가 망할 때에 국난을 당하여 죽는 사람이 하나도 없다는 것이 어찌 원통치 않겠는가?", R.drawable.expression);
//        actionMacro.add(id, 0, "내 키를 땅에서부터 재면 누구보다 작아도, 하늘로 부터 재면 누구보다 크다.", R.drawable.expression);
//        actionMacro.add(id, 0, "너 자신을 알라.", R.drawable.expression);
//        actionMacro.add(id, 0, "너는 어찌하여 닭을 잡는데 소 잡는 칼을 썼느냐?", R.drawable.expression);
//        actionMacro.add(id, 0, "너가 좋아하지 않는 사람들을 생각하는데는 단 1분도 허비하지 마라.", R.drawable.expression);
//        actionMacro.add(id, 0, "널리 배우고, 의문이 있으면 곧 묻고, 삼가 깊이 생각하며, 밝게 분변하고, 곧 이를 독실히 행하라.", R.drawable.expression);
//        actionMacro.add(id, 0, "네가 싫어하는 것을 남에게 베풀지 마라.", R.drawable.expression);
//        actionMacro.add(id, 0, "노동자들에게는 혁명 외에 희망이 없다.", R.drawable.expression);
//        actionMacro.add(id, 0, "만일 그것이 사실이라면 노동자들에게는 진정 희망이 없다.", R.drawable.expression);
//        actionMacro.add(id, 0, "노병은 죽지 않는다. 다만 사라질 뿐이다.", R.drawable.expression);
//        actionMacro.add(id, 0, "농사는 하늘이 지어주는 것이 아니라 인간의 지혜와 노력으로서 짓는 것이다.", R.drawable.expression);
//        actionMacro.add(id, 0, "누군가 망상에 시달리면 정신 이상이라고 한다. 다수가 망상에 시달리면 종교라고 한다.", R.drawable.expression);
//        actionMacro.add(id, 0, "누구나 빵을 먹을 수 있을 때까지, 아무도 케이크를 먹어서는 안 된다.", R.drawable.expression);
//        actionMacro.add(id, 0, "누구든 진실과 지식의 분야에서 자신을 판사라 생각하는 자는 신들의 웃음소리에 의해 박살날 것이다.", R.drawable.expression);
//        actionMacro.add(id, 0, "눈물을 흘리며 씨를 뿌리는 자는 기쁨으로 거두리로다.", R.drawable.expression);
//        actionMacro.add(id, 0, "늙은이들이 전쟁을 선포한다. 그러나 싸워야 하고 죽어야 하는 것은 젊은이들이다.", R.drawable.expression);
//        actionMacro.add(id, 0, "늦었다고 생각할 때가 진짜 너무 늦었다. 그러니 지금 당장 시작하라.", R.drawable.expression);

//        actionMacro.add(id, 0, "다른 벼슬은 구해도 좋으나, 목민관의 벼슬은 구해서는 안 된다.", R.drawable.expression);
//        actionMacro.add(id, 0, "달리는 기차 위에 중립은 없다.", R.drawable.expression);
//        actionMacro.add(id, 0, "답이 없는 문제도 있기 마련이다.", R.drawable.expression);
//        actionMacro.add(id, 0, "당신들은 노동자들에게 가시 돋힌 왕관을 씌울 수 없습니다. 당신들은 인류를 황금의 십자가에 못 박을 수 없습니다.", R.drawable.expression);
//        actionMacro.add(id, 0, "당신은 당신이 생각하는 대로 살아야 한다. 그러지 않으면 머지않아 당신은 사는 대로 생각하게 될 것이다.", R.drawable.expression);
//        actionMacro.add(id, 0, "당신은 전쟁에 관심이 없을 수도 있다. 그러나 전쟁은 당신에 관심이 있다.", R.drawable.expression);
//        actionMacro.add(id, 0, "당신들이 어떻게 생각하든, 역사는 우리 편이다. 당신들을 묻어버리겠다.", R.drawable.expression);
//        actionMacro.add(id, 0, "당신들에겐 시계가 있지만 우리들에겐 시간이 있다.", R.drawable.expression);
//        actionMacro.add(id, 0, "당신이 가진 생각이 딱 하나밖에 없다면, 그것만큼 위험한 것은 없다.", R.drawable.expression);
//        actionMacro.add(id, 0, "대의명분이 있다면 테러리스트라 불릴 수 없다.", R.drawable.expression);
//        actionMacro.add(id, 0, "대장부가 왜 우느냐? 옛 사람이 말하기를 ‘나를 쓰다듬으면 임금이요, 나를 학대하면 원수로다.’고 하였다. 지금 왕의 행함이 잔학하여 사람을 죽이니 백성의 원수다. 네가 그를 죽여라.", R.drawable.expression);
//        actionMacro.add(id, 0, "대중들은 작은 거짓말보다 더 큰 거짓말에 쉽게 속는다.", R.drawable.expression);
//        actionMacro.add(id, 0, "대중은 여자와 같아 자신을 지배해 줄 강력한 지도자가 나타나기를 기다린다.", R.drawable.expression);
//        actionMacro.add(id, 0, "대체 이 일을 어떻게 끝낼 수 있을까 모르겠지만, 성추행 당한 내 어린 딸과 다른 어린이들을 위해서, 그리고 그런 성도착자들을 처벌하지 않고 놓아두고자 하는 그런 이들이 있는 한 난 절대로 침묵하지 않을 것이다. 내 인생의 마지막 날까지 나는 아동 성추행범들과 싸울 것이다. 만약 나의 네 살짜리 딸조차도 보호할 수 없다면, 차라리 죽는 게 나을 것이다.", R.drawable.expression);
//        actionMacro.add(id, 0, "더 이상 내 다리는 달릴 수 없지만 나에게는 아직 두 팔이 남아 있다.", R.drawable.expression);
//        actionMacro.add(id, 0, "더 이상 추가할 것이 없을 때가 아니라 더 이상 뺄 것이 없을 때, 완벽함이 성취된다.", R.drawable.expression);
//        actionMacro.add(id, 0, "독재자에게는 폭력을 써서라도 맞서 싸워야 한다.", R.drawable.expression);
//        actionMacro.add(id, 0, "두 여자를 화해시키는 것은 유럽을 통일하는 것보다 어렵다.", R.drawable.expression);

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
