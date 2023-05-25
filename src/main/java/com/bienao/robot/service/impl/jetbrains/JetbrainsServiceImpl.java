package com.bienao.robot.service.impl.jetbrains;

import com.bienao.robot.entity.JetbrainsEntity;
import com.bienao.robot.mapper.JetbrainsMapper;
import com.bienao.robot.service.jetbrains.JetbrainsService;
import com.bienao.robot.utils.ActivateJetBrainsUtil;
import com.fooock.shodan.ShodanRestApi;
import com.fooock.shodan.model.banner.Banner;
import com.fooock.shodan.model.host.HostReport;
import io.reactivex.observers.DisposableObserver;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class JetbrainsServiceImpl implements JetbrainsService {

    @Autowired
    private JetbrainsMapper jetbrainsMapper;

    public void reptile(){
        try {

            ShodanRestApi api = new ShodanRestApi("SenyXVKG6IrgkuLzWZdOJG4qgkW9m6pH");
            api.hostSearch("Location: https://account.jetbrains.com/fls-auth").subscribe(new DisposableObserver<HostReport>() {
                @Override
                public void onNext(@NotNull HostReport hostReport) {

                    List<String> addresses = jetbrainsMapper.queryAllUrl();

                    List<Banner> banners = hostReport.getBanners();
                    for (Banner banner : banners) {
                        String url = "{protocol}://{ip}:{port}";
                        if (banner.getData().contains("account.jetbrains.com")){
                            String protocol = "http";
                            if (banner.isSslEnabled()){
                                protocol = "https";
                            }
                            String address = url.replace("{protocol}", protocol)
                                    .replace("{ip}", banner.getIpStr())
                                    .replace("{port}", String.valueOf(banner.getPort()));
                            if (!addresses.contains(address)){
                                JetbrainsEntity jetbrainsEntity = new JetbrainsEntity();
                                jetbrainsEntity.setUrl(address);
                                jetbrainsMapper.insert(jetbrainsEntity);
                            }
                        }
                    }
                }

                @Override
                public void onError(@NotNull Throwable e) {

                }

                @Override
                public void onComplete() {

                }
            });
        } catch (Exception error) {
            System.out.println("Exception: " + error.getMessage());
        }
    }

    /**
     * 筛选有效的激活服务器地址
     * @return
     */
    public List<String> getValidUrls(){
        ArrayList<String> validAddresses = new ArrayList<>();
        List<String> addresses = jetbrainsMapper.queryAllUrl();
        for (int i = 0; i < addresses.size(); i++) {
            String address = addresses.get(i);
            log.info("共{}个激活服务器，当前正在测试第{}个，激活地址{}",addresses.size(),i+1,address);
            if (ActivateJetBrainsUtil.checkServer(address)){
                validAddresses.add(address);
            }
        }
        return validAddresses;
    }

    @Override
    public void addUrls(String urls) {
        List<String> addresses = jetbrainsMapper.queryAllUrl();
        String[] split = urls.split(" ");
        List<String> list = Arrays.asList(split);
        for (String url : list) {
            if (!addresses.contains(url)){
                JetbrainsEntity jetbrainsEntity = new JetbrainsEntity();
                jetbrainsEntity.setUrl(url);
                jetbrainsMapper.insert(jetbrainsEntity);
            }
        }
    }
}
