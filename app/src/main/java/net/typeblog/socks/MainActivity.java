//package net.typeblog.socks;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.os.PersistableBundle;
//
///**
// * Copyright (C), 2016-2022
// * Author: 超人迪加
// * Date: 2022/3/27 12:07 下午
// */
//public class MainActivity extends Activity {
//    @Override
//    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
//        super.onCreate(savedInstanceState, persistentState);
//    }
//
//    @Override public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main);
//
//        Utility.extractFile(this);
//
//        getFragmentManager().beginTransaction().replace(R.id.frame, new ProfileFragment()).commit()
//    }
//}