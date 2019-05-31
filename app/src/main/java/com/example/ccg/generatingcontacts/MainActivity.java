package com.example.ccg.generatingcontacts;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button button1;
    Button button2;
    Button button3;
    Button button4;
    // 生成的联系人个数
    int count = 10;
    // 默认app
    private String defaultSmsPkg;
    // my app
    private String mySmsPkg;
    // 随机生成的短信总条数
    int sms_count = 3;
    // 短信uri
    private final Uri SMS_INBOX = Uri.parse("content://sms");
    private final Uri SMS_ADDRESS = Uri.parse("content://mms-sms/canonical-addresses");
    private final Uri SMS_THREADS = Uri.parse("content://mms-sms/conversations");

    final String tag = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button1 = (Button) findViewById(R.id.button_add);
        button2 = (Button) findViewById(R.id.button_delete);
        button3 = (Button) findViewById(R.id.sms_add);
        button4 = (Button) findViewById(R.id.sms_delete);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
        defaultSmsPkg = Telephony.Sms.getDefaultSmsPackage(this);
        mySmsPkg = this.getPackageName();


        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
                permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new
                    String[]{ Manifest.permission.WRITE_CONTACTS}, 1);
        }else{
            add();
//            addContact("test", "13430928877");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_add:
                add();
                break;
            case R.id.button_delete:
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest
                        .permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new
                            String[]{ Manifest.permission.READ_CONTACTS}, 3);
                } else {
                    delete();
                }
                break;
            case R.id.sms_add:{
                addToInsertSms();
            }
                break;
            default:
                break;
        }
    }

    /**
     * 批量生成联系人
     */
    private void add() {
        Toast.makeText(this, "开始添加联系人数据", Toast.LENGTH_SHORT).show();
        // 随机数
        Random random = new Random();
        for(int i = 0;i < count;i++) {
            //产生0-10的数字
            int number = random.nextInt(10);
            String name = getRandomString(number);
            String phoneNum = getRandomPhone();
            addContact(name, phoneNum);
        }
        Toast.makeText(this, "联系人数据添加成功", Toast.LENGTH_SHORT).show();
    }

    /**
     * 批量生成短信信息
     */
    private void addToInsertSms() {
        if(!defaultSmsPkg.equals(mySmsPkg)) {
            // 如果这个App不是默认的Sms App，则修改成默认的SMS APP
            // 因为从Android 4.4开始，只有默认的SMS APP才能对SMS数据库进行处理
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT); // android.provider.Telephony.ACTION_CHANGE_DEFAULT
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, mySmsPkg);
            startActivityForResult(intent, 1);
        }
        for(int i = 0;i < sms_count;i++) {
            addSmsToPhone(i);
        }
        // 对短信数据库处理结束，恢复原来的默认SMS APP
        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, defaultSmsPkg);
        startActivity(intent);
        Log.d(tag, "Recover default SMS App");
    }
    /**
     * 生成联系人
     * @param name
     * @param phoneNum
     */
    private void addContact(String name, String phoneNum) {
        ContentValues values = new ContentValues();
        // content://com.android.contacts/raw_contacts
        Uri rawContactUri = getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);
        // 获取id
        long rawContactId = ContentUris.parseId(rawContactUri);
        values.clear();

        // raw_contact_id
        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        // 内容类型-mimetype, vnd.android.cursor.item/name
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName
            .CONTENT_ITEM_TYPE);
        // 联系人名字-data2,
        values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                name);
        // 向联系人URI添加联系人名字 content://com.android.contacts/data
        getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
        values.clear();

        // raw_contact_id
        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        // mimetype, vnd.android.cursor.item/phone_v2
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds
                .Phone.CONTENT_ITEM_TYPE);
        // 联系人的电话号码-data1
        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNum);
        // 电话类型-data2, 2
        values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds
                .Phone.TYPE_MOBILE);
        // 向联系人电话号码URI添加电话号码 content://com.android.contacts/data
        getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
        values.clear();
    }

    /**
     * 删除全部联系人
     */
    private void delete() {
        Cursor cursor = getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        while (cursor.moveToNext()) {
            // 获取ID
            String rawId = cursor.getString(cursor.getColumnIndex(
                    ContactsContract.Contacts._ID
            ));
            // 删除
            String where = ContactsContract.Contacts._ID + "=?";
            String[] whereparams = new String[]{rawId};
            getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI, where, whereparams);
        }
        Toast.makeText(this, "联系人全部删除成功", Toast.LENGTH_SHORT).show();
    }

    /**
     * 写入短信信息
     */
    @Deprecated
    private void addSms() {
        Toast.makeText(this, "开始添加短信信息数据", Toast.LENGTH_SHORT).show();
        ContentValues values = new ContentValues();
        // canonical_addresses表插入
        String phoneNum = getRandomPhone();
        values.put("address", phoneNum);
        Uri canonicalAddressesUri = getContentResolver().insert(SMS_ADDRESS, values);
        long canonicalAddressesid = ContentUris.parseId(canonicalAddressesUri);
        values.clear();

        // threads表插入
        Random random = new Random();
        int messageCount = random.nextInt(sms_count) + 1; // 短信总条数
        String snippet = getRandomString(5); // 在最前面显示的短信
        int snippetCs = 0; // snippet的编码方式，彩信：UTF-8为106，短信为0
        int read = random.nextInt(1); // 是否有未读信息：0-未读，1-已读
        int type = 0; // 会话类型，0-普通会话（只有一个接收者），1-广播会话（多个接收者）
        int error = 0; // 发送失败的消息（type=5）的数量
        int hasAttachment = 0; // 是否有附件：0-无，1-有
        values.put("date", System.currentTimeMillis());
        values.put("message_count", messageCount);
        values.put("recipient_ids", canonicalAddressesid);
        values.put("snippet", snippet);
        values.put("snippet_cs", snippetCs);
        values.put("read", read);
        values.put("type", type);
        values.put("error", error);
        values.put("has_attachment", hasAttachment);
        Uri threadsUri = getContentResolver().insert(SMS_THREADS, values);
        long threadId = ContentUris.parseId(threadsUri);
        values.clear();

        // sms表插入
        int protocol = 0; // 协议，分为：0-SMS_RPOTO，1-MMS_PROTO。成功发送后设置。
        int status = 0; // 状态：-1默认值，0-complete，64-pending，128-failed
        int typeSms = 1; // ALL=0;INBOX(接受)=1;SENT（发送）=2;DRAFT=3;OUTBOX=4;FAILED=5;QUEUED=6;
        int reply_path_present = 0; // 	发短信为空，收到的为0
        String body = getRandomString(10); // 短信内容
        int locked = 0; // 此条短信是否已由用户锁定，0-未锁定，1-已锁定
        int seen = 0; // 用于指明该消息是否已被用户看到（非阅读，点开会话列表即可，不用打开会话），仅对收到的消息有用
        values.put("thread_id ", threadId);
        values.put("address", phoneNum); // 对方短信号码
        values.put("date", System.currentTimeMillis());
        values.put("protocol", protocol);
        values.put("read", read);
        values.put("status", status);
        values.put("type", typeSms);
        values.put("reply_path_present", reply_path_present);
        values.put("body", body);
        values.put("locked", locked);
        values.put("seen", seen);
        getContentResolver().insert(SMS_INBOX, values);
        values.clear();
        Toast.makeText(this, "短信信息数据添加成功", Toast.LENGTH_SHORT).show();
    }

    @Deprecated
    private void sentSms() {
        Toast.makeText(this, "开始发送短信信息数据", Toast.LENGTH_SHORT).show();
        Thread t = new Thread(){
            @Override
            public void run() {
                ContentResolver cr = getContentResolver();
                ContentValues values = new ContentValues();
                values.put("address", 95533);
                values.put("type", 1);
                values.put("date", System.currentTimeMillis());
                values.put("body", "您尾号为9999的信用卡收到1,000,000RMB转账，请注意查收");
                cr.insert(Uri.parse("content://sms"), values);
                Toast.makeText(MainActivity.this, "发送短信信息数据成功", Toast.LENGTH_SHORT).show();
            }
        };
        t.start();
    }

    /**
     * 取代默认app应用来写入短息信息
     */
    private void addSmsToPhone(int i) {
        if(mySmsPkg.equals(Telephony.Sms.getDefaultSmsPackage(MainActivity.this))) {
            String phoneNum = getRandomPhone();
            StringBuffer sb = new StringBuffer();
            sb.append("测试用例").append(String.valueOf(i));
            String mMsg = sb.toString();
            Log.d(tag, "My App is default SMS App.");
            // 对短信数据库进行处理
            ContentResolver resolver = getContentResolver();
            ContentValues values = new ContentValues();
            values.put(Telephony.Sms.ADDRESS, phoneNum);
            values.put(Telephony.Sms.DATE, System.currentTimeMillis());
            long dateSent = System.currentTimeMillis() - 5000;
            values.put(Telephony.Sms.DATE_SENT, dateSent);
            values.put(Telephony.Sms.READ, false);
            values.put(Telephony.Sms.SEEN, false);
            values.put(Telephony.Sms.STATUS, Telephony.Sms.STATUS_COMPLETE);
            values.put(Telephony.Sms.BODY, mMsg);
            values.put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_INBOX);
            resolver.insert(Telephony.Sms.CONTENT_URI, values);
/*            if(uri != null) {
                long uriId = ContentUris.parseId(uri);
            }*/
            Toast.makeText(MainActivity.this, "短信信息添加完成",
                    Toast.LENGTH_SHORT).show();

    }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    add();
                }else{
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;/*
            case 2:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sentSms();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;*/
            case 3:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    delete();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case 1:
                if(resultCode == RESULT_OK) {
                    addToInsertSms();
                }
                break;
            default:

        }

    }

    /**
     * 产生随机字符串
     * @param length
     * @return
     */
    public static String getRandomString(int length){
        //定义一个字符串（A-Z，a-z，0-9）即62位；
        String str="zxcvbnmlkjhgfdsaqwertyuiopQWERTYUIOPASDFGHJKLZXCVBNM1234567890";
        //由Random生成随机数
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        //长度为几就循环几次
        for(int i = 0;i < length;i++){
            //产生0-61的数字
            int number = random.nextInt(62);
            //将产生的数字通过length次承载到sb中
            sb.append(str.charAt(number));
        }
        //将承载的字符转换成字符串
        return sb.toString();
    }

    /**
     * 产生随机11位数字
     * @return
     */
    public static String getRandomPhone(){
        //定义一个字符串（0-9）即10位；
        String str="0123456789";
        //由Random生成随机数
        Random random=new Random();
        StringBuffer sb=new StringBuffer();
        //长度为几就循环几次
        for(int i = 0;i < 11;++i){
            //产生0-9的数字
            int number=random.nextInt(10);
            //将产生的数字通过length次承载到sb中
            sb.append(str.charAt(number));
        }
        //将承载的字符转换成字符串
        return sb.toString();
    }
}
