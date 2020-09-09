package com.snowapp.jjfunny;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.snowapp.jjfunny.model.Destination;
import com.snowapp.jjfunny.model.User;
import com.snowapp.jjfunny.ui.login.UserManager;
import com.snowapp.jjfunny.utils.AppConfig;
import com.snowapp.jjfunny.utils.NavGraphBuilder;
import com.snowapp.jjfunny.view.AppBottomBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private NavController navController;
    private AppBottomBar navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navView = findViewById(R.id.nav_view);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = NavHostFragment.findNavController(fragment);
//        NavigationUI.setupWithNavController(navView, navController);

        // 替代 xml 中的 app:navGraph="@navigation/mobile_navigation" 资源引用
        NavGraphBuilder.build(MainActivity.this, navController, fragment.getId());

        navView.setOnNavigationItemSelectedListener(this);
    }

    /**
     * @date 2020-08-19
     * @author snow
     * @param menuItem 底部导航栏按钮
     * @return true 选中；false 未选中
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        HashMap<String, Destination> destConfig = AppConfig.getDestConfig();
        Iterator<Map.Entry<String, Destination>> iterator = destConfig.entrySet().iterator();
        //遍历 target destination 是否需要登录拦截
        while (iterator.hasNext()) {
            Map.Entry<String, Destination> entry = iterator.next();
            Destination value = entry.getValue();
            if (value != null && !UserManager.get().isLogin() && value.needLogin && value.id == menuItem.getItemId()) {
                UserManager.get().login(this).observe(this, new Observer<User>() {
                    @Override
                    public void onChanged(User user) {
                        navView.setSelectedItemId(menuItem.getItemId());
                    }
                });
                return false;
            }
        }
        // 因为在 AppBottomBar.java 中为 menuItem 设置了 itemId，所以这里可以直接获取
        navController.navigate(menuItem.getItemId());
        // 如果 title 为空，则不选中，进行着色。反之，则选中，不着色
        return !TextUtils.isEmpty(menuItem.getTitle());
    }
}
