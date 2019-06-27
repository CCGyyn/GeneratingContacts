package com.example.ccg.generatingcontacts;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

/**
 * @author cai_gp
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int PUSH_NOTIFICATION_ID = (0x001);
    private static final String PUSH_CHANNEL_ID = "PUSH_NOTIFY_ID";
    private static final String PUSH_CHANNEL_NAME = "PUSH_NOTIFY_NAME";
    private Button contactAddButton;
    private Button contactDeleteButton;
    private Button smsAddButton;
    private Button smsDeleteButton;
    private Button infoAddButton;
    private Button callRecordsAddButton;
    private Button callRecordsDeleteButton;

    Button smsItemAddButton;
    EditText contactCount;
    EditText smsCount;
    EditText callRecordsCount;
    EditText smsItem;
    private final int CALL_WRITE_REQUEST_CODE = 4;
    private final int CALL_READ_REQUEST_CODE = 5;
    int sms_item = 200;
    int call_records_count = 3000;
    /**
     * 生成的联系人个数
     */
    int count = 3000;
    /**
     * 默认app
     */
    private String defaultSmsPkg;
    /**
     * my app
     */
    private String mySmsPkg;
    /**
     * 随机生成的短信总条数
     */
    int sms_count = 3000;
    /**
     * 随机生成通知条数
     */
    int info_count = 3000;
    /**
     * 短信uri
     */
    private final Uri SMS_INBOX = Uri.parse("content://sms");
    private final Uri SMS_ADDRESS = Uri.parse("content://mms-sms/canonical-addresses");
    private final Uri SMS_THREADS = Uri.parse("content://mms-sms/conversations");

    final String tag = "MainActivity";
    private EditText infoCount;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private Notification notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contactAddButton = (Button) findViewById(R.id.contact_add);
        contactDeleteButton = (Button) findViewById(R.id.contact_delete);
        smsAddButton = (Button) findViewById(R.id.sms_add);
        smsDeleteButton = (Button) findViewById(R.id.sms_delete);
        infoAddButton = (Button) findViewById(R.id.info_add);
        callRecordsAddButton = (Button)findViewById(R.id.call_records_add);
        callRecordsDeleteButton = (Button)findViewById(R.id.call_records_delete);
        smsItemAddButton = (Button) findViewById(R.id.sms_item_add);

        callRecordsCount = (EditText) findViewById(R.id.call_records_count);
        contactCount = (EditText) findViewById(R.id.contact_count);
        smsCount = (EditText) findViewById(R.id.sms_count);
        infoCount = (EditText) findViewById(R.id.info_count);
        smsItem = (EditText) findViewById(R.id.sms_item);

        contactAddButton.setOnClickListener(this);
        contactDeleteButton.setOnClickListener(this);
        smsAddButton.setOnClickListener(this);
        smsDeleteButton.setOnClickListener(this);
        infoAddButton.setOnClickListener(this);
        callRecordsAddButton.setOnClickListener(this);
        callRecordsDeleteButton.setOnClickListener(this);
        smsItemAddButton.setOnClickListener(this);
        // defaultSmsPkg = Telephony.Sms.getDefaultSmsPackage(this); // 如果一开始就被改成默认应用的话就无效了，就不会改回原来的信息默认应用。
        mySmsPkg = this.getPackageName();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.contact_add:
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
                        permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this, new
                            String[]{ Manifest.permission.WRITE_CONTACTS}, 1);
                }else{
                    Toast.makeText(this, "开始添加联系人数据", Toast.LENGTH_SHORT).show();
                    /*Method method = null;
                    try {
                        method = MainActivity.class.getMethod("add");
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    methodByThread(method);*/
                    if(!contactCount.getText().toString().isEmpty()) {
                        count = Integer.valueOf(contactCount.getText().toString());
                    }
                    addByThread();
                }
                break;
            case R.id.contact_delete:
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest
                        .permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new
                            String[]{ Manifest.permission.READ_CONTACTS}, 3);
                } else {
                    Toast.makeText(this, "开始删除联系人数据", Toast.LENGTH_SHORT).show();
                    deleteByThread();
                }
                break;
            case R.id.sms_add:{
                // 防止中途出现'this' is not available，没有实时获取当前默认应用
                defaultSmsPkg = Telephony.Sms.getDefaultSmsPackage(this);
                if(!smsCount.getText().toString().isEmpty()) {
                    sms_count = Integer.valueOf(smsCount.getText().toString());
                }
                addToInsertSms();
            }
                break;
            case R.id.sms_delete:{
                defaultSmsPkg = Telephony.Sms.getDefaultSmsPackage(this);
                deleteSms();
            }
                break;
            case R.id.info_add:
                Toast.makeText(this, "开始添加通知数据", Toast.LENGTH_SHORT).show();
                if(!infoCount.getText().toString().isEmpty()) {
                    info_count = Integer.valueOf(infoCount.getText().toString());
                }else{
                    info_count =3000;
                }
                getInfoMations();
                break;
            case R.id.call_records_add:{
                if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_CALL_LOG)
                        !=PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_CALL_LOG}, CALL_WRITE_REQUEST_CODE);
                }else {
                    if(!callRecordsCount.getText().toString().isEmpty()) {
                        call_records_count = Integer.valueOf(callRecordsCount.getText().toString());
                    }
                    addCallRecordsByThread();
                }
                break;
            }
            case R.id.call_records_delete:{
                /*if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED)
                        &&(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED)){
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_CALL_LOG}, CALL_WRITE_REQUEST_CODE);
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_CALL_LOG}, CALL_READ_REQUEST_CODE);
                }*/
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_CALL_LOG}, CALL_READ_REQUEST_CODE);
                } else {
                    deleteCallRecordsByThread();
                }
                break;
            }
            case R.id.sms_item_add:{
                defaultSmsPkg = Telephony.Sms.getDefaultSmsPackage(this);
                if(!smsCount.getText().toString().isEmpty()) {
                    sms_count = Integer.valueOf(smsCount.getText().toString());
                }
                if(!smsItem.getText().toString().isEmpty()){
                    sms_item = Integer.valueOf(smsItem.getText().toString());
                }
                addItemToInsertSms();
            }
            break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    addByThread();
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
                    deleteByThread();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            case CALL_WRITE_REQUEST_CODE:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(!callRecordsCount.getText().toString().isEmpty()) {
                        call_records_count = Integer.valueOf(callRecordsCount.getText().toString());
                    }
                    addCallRecordsByThread();
                    Log.d("permissions:",permissions.toString());
                } else {
                    Toast.makeText(this, "You denied the call log write permission", Toast.LENGTH_SHORT).show();
                    Log.d("permissions:",permissions.toString());
                }
                break;
            }
            case CALL_READ_REQUEST_CODE:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    deleteCallRecordsByThread();
                    Log.d("permissions:",permissions.toString());
                } else {
                    Toast.makeText(this, "You denied the call log read permission", Toast.LENGTH_SHORT).show();
                    Log.d("permissions:",permissions.toString());
                }
                break;
            }
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case 1:
                if(resultCode == RESULT_OK) {
                    addToInsertSmsByThread();
                }
                break;
            case 2:
                if(resultCode == RESULT_OK) {
                    deleteSmsToPhoneByThread();
                }
                break;
            case 3:
                if(resultCode == RESULT_OK){
                    addItemToInsertSmsByThread();
                }
                break;
            default:

        }

    }

    /**
     * 开启子线程进行批量生成联系人
     */
    public void addByThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                add();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "数据添加成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    /**
     * 开启子线程执行耗时方法
     * @param method
     * @throws Exception
     * 可能是使用的时候有问题
     */
    @Deprecated
    private void methodByThread(final Method method){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    method.invoke(MainActivity.class);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "数据添加成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    /**
     * 批量生成联系人
     */
    public void add() {
        // 随机数
        Random random = new Random();
        for(int i = 0;i < count;i++) {
            //产生0-10的数字
            int number = random.nextInt(10);
            String name = getRandomString(number);
            String phoneNum = getRandomPhone();
            addContact(name, phoneNum);
        }

    }

    /**
     * 生成联系人
     * @param name
     * @param phoneNum
     */
    public void addContact(String name, String phoneNum) {
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
     * 开启子线程进行批量删除联系人
     */
    public void deleteByThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                delete();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "数据删除成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    /**
     * 删除全部联系人
     */
    public void delete() {
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
    }

    /**
     *开启子线程进行批量生成短信信息
     */
    public void addToInsertSmsByThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "开始添加短信数据", Toast.LENGTH_SHORT).show();
                    }
                });
                for(int i = 0;i < sms_count;i++) {
                    addSmsToPhone(i);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "数据插入成功", Toast.LENGTH_SHORT).show();
                        // 对短信数据库处理结束，恢复原来的默认SMS APP
                        reductionApp();
                    }
                });
            }
        }).start();
    }

    /**
     * 批量生成短信信息
     */
    public void addToInsertSms() {
        if(!defaultSmsPkg.equals(mySmsPkg)) {
            // 如果这个App不是默认的Sms App，则修改成默认的SMS APP
            // 因为从Android 4.4开始，只有默认的SMS APP才能对SMS数据库进行处理
            // android.provider.Telephony.ACTION_CHANGE_DEFAULT
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            // package
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, mySmsPkg);
            startActivityForResult(intent, 1);
        } else {
            addToInsertSmsByThread();
        }

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
     * 默认应用不需要申请权限，取代默认应用会弹窗进行确认
     */
    public void addSmsToPhone(int i) {
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
    }
    }

    /**
     * 开启子线程进行批量删除短信
     */
    public void deleteSmsToPhoneByThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "开始删除短信数据", Toast.LENGTH_SHORT).show();
                    }
                });
                deleteSmsToPhone();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "数据删除成功", Toast.LENGTH_SHORT).show();
                        // 对短信数据库处理结束，恢复原来的默认SMS APP
                        reductionApp();
                    }
                });
            }
        }).start();
    }

    /**
     * 批量删除短信信息
     */
    public void deleteSms() {
        if(!defaultSmsPkg.equals(mySmsPkg)) {
            // 如果这个App不是默认的Sms App，则修改成默认的SMS APP
            // 因为从Android 4.4开始，只有默认的SMS APP才能对SMS数据库进行处理
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT); // android.provider.Telephony.ACTION_CHANGE_DEFAULT
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, mySmsPkg);
            startActivityForResult(intent, 2);
        } else {
            deleteSmsToPhoneByThread();
        }
    }

    /**
     * 取代默认app应用来删除短息信息
     */
    public void deleteSmsToPhone() {
        if(mySmsPkg.equals(Telephony.Sms.getDefaultSmsPackage(MainActivity.this))) {
            Cursor cursor = getContentResolver().query(
                    Telephony.Sms.CONTENT_URI,
                    null,
                    null,
                    null,
                    null
            );
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(Telephony.Sms._ID));
                String where = Telephony.Sms._ID + "=?";
                String[] whereParams = new String[] {id};
                getContentResolver().delete(
                        Telephony.Sms.CONTENT_URI,
                        where,
                        whereParams
                        );
            }
        }
    }

    /**
     * 将短信默认应用修改为信息
     */
    public void reductionApp() {
        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        if(!defaultSmsPkg.equals(mySmsPkg)) {
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, defaultSmsPkg);
        } else {
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, "com.android.mms");
        }
        startActivity(intent);
        Log.d(tag, "Recover default SMS App");
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

    @Override
    public synchronized ComponentName startForegroundServiceAsUser(Intent service, UserHandle user) {
        return null;
    }

    /**
     * 生成通知
     */
    public void getInfoMation(int i){

//        Intent notificationIntent = new Intent(MainActivity.this, MainActivity.class);
////        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
////        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 10086, notificationIntent, 0);
        builder.setContentTitle("系统通知:")//设置通知栏标题
//                .setContentIntent(pendingIntent) //设置通知栏点击意图
                .setContentText("数字"+i)
//                .setTicker(msg.getDisplayMessageBody()) //通知首次出现在通知栏，带上升动画效果的
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setSmallIcon(R.mipmap.ic_launcher)//设置通知小ICON
                .setChannelId(PUSH_CHANNEL_ID)
                .setDefaults(Notification.DEFAULT_ALL);

        notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        if (notificationManager != null) {
            notificationManager.notify(i, notification);
        }

    }

    /**
     * 批量生成通知
     */
    public void getInfoMations(){
        notificationManager = (NotificationManager) MainActivity.this.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(PUSH_CHANNEL_ID, PUSH_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        builder = new NotificationCompat.Builder(MainActivity.this,PUSH_CHANNEL_ID);
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "开始生成通知数据", Toast.LENGTH_SHORT).show();
                    }
                });
                for(int i = 0;i < info_count;i++) {
                    getInfoMation(i);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "数据生成成功", Toast.LENGTH_SHORT).show();
                        // 对短信数据库处理结束，恢复原来的默认SMS APP
                    }
                });
            }
        }).start();
    }

    /**
     * 开启线程批量添加通话记录
     * @Autor chen hao
     */
    public void addCallRecordsByThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "开始添加通话记录", Toast.LENGTH_SHORT).show();
                    }
                });
                addCallRecords();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "通话记录添加成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    /**
     * 批量添加通话记录
     * @Autor chen hao
     */
    public void addCallRecords(){
        // 随机数
        Random random = new Random();
        OneCall call = new OneCall();
        for(int i = 0; i < call_records_count; i++) {
            int time = random.nextInt(15);
            call.setNumber(getRandomPhone());
            call.setType(Integer.toString(random.nextInt(2)+1));
            call.setDuration(Integer.toString(time));
            addOneCallRecord(call);
        }
    }

    /**
     * 添加一条通话记录
     * @Autor chen hao
     */
    public void addOneCallRecord(OneCall call){
        ContentValues values = new ContentValues();
        values.clear();
        values.put(CallLog.Calls.NUMBER, call.getNumber());
        values.put(CallLog.Calls.DATE, System.currentTimeMillis() );
        values.put(CallLog.Calls.DURATION, call.getDuration());
        values.put(CallLog.Calls.TYPE, call.getType());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_CALL_LOG}, CALL_WRITE_REQUEST_CODE);
        }
        getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);
        values.clear();
    }

    /**
     * 开启子线程进行批量删除通话记录
     * @Autor  chen hao
     */
    public void deleteCallRecordsByThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                deleteCallRecords();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "所有通话记录删除成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    /**
     * 删除全部通话记录
     * @Autor  chen hao
     */
    public void deleteCallRecords() {
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED)
                &&(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED)){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_CALL_LOG}, CALL_WRITE_REQUEST_CODE);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CALL_LOG}, CALL_READ_REQUEST_CODE);
        }
        Cursor cursor = getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        while (cursor.moveToNext()) {
            // 获取ID
            String rawId = cursor.getString(cursor.getColumnIndex(
                    CallLog.Calls._ID
            ));
            // 删除
            String where = CallLog.Calls._ID + "=?";
            String[] whereparams = new String[]{rawId};
            getContentResolver().delete(CallLog.Calls.CONTENT_URI, where, whereparams);
        }
    }

    /** 批量生成多条短信内容
     *
     * @author huang_js
     */
    public void addItemToInsertSms() {
        if(!defaultSmsPkg.equals(mySmsPkg)) {
            // 如果这个App不是默认的Sms App，则修改成默认的SMS APP
            // 因为从Android 4.4开始，只有默认的SMS APP才能对SMS数据库进行处理
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, mySmsPkg);
            startActivityForResult(intent, 3);
        } else {
            addItemToInsertSmsByThread();
        }
    }

    /**开启子线程生成多条短信内容,默认每个200条短信内容
     *
     * @author huang_js
     *
     */
    public void addItemToInsertSmsByThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"开始添加短信内容",Toast.LENGTH_SHORT).show();
                    }
                });
                addItemToPhone();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"数据添加成功",Toast.LENGTH_SHORT).show();
                        reductionApp();
                    }
                });
            }
        }).start();
    }

    /** 取代默认App应用来写入多条短信内容
     *
     * @author huang_js
     */
    public void addItemToPhone(){
        if(mySmsPkg.equals(Telephony.Sms.getDefaultSmsPackage(MainActivity.this))){
            String phoneNum = getRandomPhone();
            StringBuffer sb;
            Random random = new Random();
            Log.d(tag,"My App is default SMS App.");
            //对短信数据库进行处理
            ContentResolver resolver = getContentResolver();
            ContentValues values = new ContentValues();
            values.put(Telephony.Sms.ADDRESS,phoneNum);
            values.put(Telephony.Sms.DATE, System.currentTimeMillis());
            long dateSent = System.currentTimeMillis() -5000;
            values.put(Telephony.Sms.DATE_SENT,dateSent);
            values.put(Telephony.Sms.READ,false);
            values.put(Telephony.Sms.SEEN,false);
            values.put(Telephony.Sms.STATUS,Telephony.Sms.STATUS_COMPLETE);
            for(int j=0; j < sms_item * 2; j++){
                //生成随机短信内容
                if(j % 2 == 0) {
                    values.put(Telephony.Sms.TYPE,Telephony.Sms.MESSAGE_TYPE_INBOX);
                } else {
                    values.put(Telephony.Sms.TYPE,Telephony.Sms.MESSAGE_TYPE_SENT);
                }
                sb = new StringBuffer("");
                sb.append("短信内容：").append(getRandomString(random.nextInt(8)));
                values.put(Telephony.Sms.BODY,sb.toString());
                resolver.insert(Telephony.Sms.CONTENT_URI,values);
            }
        }
    }
}
