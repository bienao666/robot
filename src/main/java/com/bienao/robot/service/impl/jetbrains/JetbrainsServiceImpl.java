package com.bienao.robot.service.impl.jetbrains;

import com.bienao.robot.entity.JetbrainsEntity;
import com.bienao.robot.mapper.JetbrainsMapper;
import com.bienao.robot.service.jetbrains.JetbrainsService;
import com.bienao.robot.utils.ActivateJetBrainsUtil;
import com.fooock.shodan.ShodanRestApi;
import com.fooock.shodan.model.banner.Banner;
import com.fooock.shodan.model.host.HostReport;
import io.reactivex.observers.DisposableObserver;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
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
        for (String address : addresses) {
            if (ActivateJetBrainsUtil.checkServer(address)){
                validAddresses.add(address);
            }
        }
        return validAddresses;
    }
}
