package eu.se_bastiaan.popcorntimeremote.rpc;

import android.content.Context;
import android.util.Log;

import com.google.gson.internal.LinkedTreeMap;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.future.ResponseFuture;

import java.util.ArrayList;
import java.util.Arrays;

import eu.se_bastiaan.popcorntimeremote.Constants;
import eu.se_bastiaan.popcorntimeremote.utils.Version;

public class PopcornTimeRpcClient {

    private Context mContext;
    private String mIpAddress, mPort, mUrl, mUsername, mPassword, mVersion;

    public enum RequestId { PING, UP, DOWN, LEFT, RIGHT, ENTER, BACK, QUALITY, NEXT_SEASON, PREV_SEASON, TOGGLE_PLAY, TOGGLE_TABS, TOGGLE_FULLSCREEN, TOGGLE_FAVOURITE, TOGGLE_WATCHED, TOGGLE_MUTE, SET_VOLUME, GET_VOLUME, FILTER_GENRE, FILTER_SORTER, FILTER_TYPE, FILTER_SEARCH, CLEAR_SEARCH, SEEK, GET_VIEWSTACK, GET_SUBTITLES, SET_SUBTITLE, LISTENNOTIFICATIONS }

    public PopcornTimeRpcClient(Context context, String ipAdress, String port, String username, String password) {
        mContext = context;
        if(Constants.LOG_ENABLED) Ion.getDefault(mContext).configure().setLogging("IonLogs", Log.DEBUG);
        mIpAddress = ipAdress;
        mPort = port;
        mUsername = username;
        mPassword = password;
        mVersion = "0.0.0";

        checkUrl();

        mUrl = mIpAddress + ":" + mPort + "/";
    }

    public void setVersion(String version) {
        mVersion = version;
    }

    public String getVersion() {
        return mVersion != null ? mVersion : "0.0.0";
    }

    public ResponseFuture<RpcResponse> ping(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("ping");
        request.id = RequestId.PING.ordinal();
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> up(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("up");
        request.id = RequestId.UP.ordinal();
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> down(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("down");
        request.id = RequestId.DOWN.ordinal();
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> left(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("left");
        request.id = RequestId.LEFT.ordinal();
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> right(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("right");
        request.id = RequestId.RIGHT.ordinal();
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> enter(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("enter");
        request.id = RequestId.ENTER.ordinal();
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> back(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("back");
        request.id = RequestId.BACK.ordinal();
        return request(request, callback);
    }


    public ResponseFuture<RpcResponse> toggleQuality(FutureCallback<RpcResponse> callback) {
        RpcRequest request;
        if(Version.compare(mVersion, "0.0.0")) {
            request = new RpcRequest("togglequality");
        } else {
            request = new RpcRequest("quality");
        }
        request.id = RequestId.QUALITY.ordinal();
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> nextSeason(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("nextseason");
        request.id = RequestId.NEXT_SEASON.ordinal();
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> prevSeason(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("previousseason");
        request.id = RequestId.PREV_SEASON.ordinal();
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> togglePlay(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("toggleplaying");
        request.id = RequestId.TOGGLE_PLAY.ordinal();
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> toggleTabs(FutureCallback<RpcResponse> callback) {
        RpcRequest request;
        if(Version.compare(mVersion, "0.0.0")) {
            request = new RpcRequest("toggletab");
        } else {
            request = new RpcRequest("togglemoviesshows");
        }
        request.id = RequestId.TOGGLE_TABS.ordinal();
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> toggleFullscreen(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("togglefullscreen");
        request.id = RequestId.TOGGLE_FULLSCREEN.ordinal();
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> toggleFavourite(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("togglefavourite");
        request.id = RequestId.TOGGLE_FAVOURITE.ordinal();
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> toggleWatched(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("togglewatched");
        request.id = RequestId.TOGGLE_WATCHED.ordinal();
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> toggleMute(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("togglemute");
        request.id = RequestId.TOGGLE_MUTE.ordinal();
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> setVolume(Double volume, FutureCallback<RpcResponse> callback) {
        RpcRequest request;
        if(Version.compare(mVersion, "0.0.0")) {
            request = new RpcRequest("volume", volume);
        } else {
            request = new RpcRequest("setvolume", Arrays.asList(volume));
        }
        request.id = RequestId.SET_VOLUME.ordinal();
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> getVolume(Double volume, FutureCallback<RpcResponse> callback) {
        RpcRequest request;
        if(Version.compare(mVersion, "0.0.0")) {
            request = new RpcRequest("volume", volume);
        } else {
            RpcResponse response = new RpcResponse();
            LinkedTreeMap<String, Object> map = new LinkedTreeMap<String, Object>();
            map.put("volume", volume);
            response.result = map;
            callback.onCompleted(null, response);
            return null;
        }
        request.id = RequestId.GET_VOLUME.ordinal();
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> filterGenre(String genre, FutureCallback<RpcResponse> callback) {
        RpcRequest request = new RpcRequest("filtergenre", genre);
        request.id = RequestId.FILTER_GENRE.ordinal();
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> filterSorter(String sorter, FutureCallback<RpcResponse> callback) {
        RpcRequest request = new RpcRequest("filtersorter", sorter);
        request.id = RequestId.FILTER_SORTER.ordinal();
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> filterType(String type, FutureCallback<RpcResponse> callback) {
        RpcRequest request = new RpcRequest("filtertype", type);
        request.id = RequestId.FILTER_TYPE.ordinal();
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> filterSearch(String searchTerm, FutureCallback<RpcResponse> callback) {
        RpcRequest request;
        if(Version.compare(mVersion, "0.0.0")) {
            request = new RpcRequest("filtersearch", searchTerm);
        } else {
            request = new RpcRequest("filtersearch", Arrays.asList(searchTerm));
        }
        request.id = RequestId.FILTER_SEARCH.ordinal();
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> clearSearch(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("clearsearch");
        request.id = RequestId.CLEAR_SEARCH.ordinal();
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> seek(Integer seconds, FutureCallback<RpcResponse> callback) {
        RpcRequest request;
        if(Version.compare(mVersion, "0.0.0")) {
            request = new RpcRequest("seek", seconds);
        } else {
            request = new RpcRequest("seek", Arrays.asList(seconds));
        }
        request.id = RequestId.SEEK.ordinal();
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> getViewstack(final FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("getviewstack");
        request.id = RequestId.GET_VIEWSTACK.ordinal();
        if(Version.compare(mVersion, "0.0.0")) {
            return request(request, callback);
        } else {
            return request(request, new FutureCallback<RpcResponse>() {
                @Override
                public void onCompleted(Exception e, RpcResponse result) {
                    if (e == null && result != null && result.result != null) {
                        ArrayList list = (ArrayList) result.result;
                        LinkedTreeMap<String, Object> map = new LinkedTreeMap<String, Object>();
                        map.put("viewstack", list.get(0));
                        result.result = map;
                    }
                    callback.onCompleted(e, result);
                }
            });
        }
    }

    public ResponseFuture<RpcResponse> getSubtitles(final FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("getsubtitles");
        request.id = RequestId.GET_SUBTITLES.ordinal();
        if(Version.compare(mVersion, "0.0.0")) {
            return request(request, callback);
        } else {
            return request(request, new FutureCallback<RpcResponse>() {
                @Override
                public void onCompleted(Exception e, RpcResponse result) {
                    if (e == null && result != null && result.result != null) {
                        ArrayList list = (ArrayList) result.result;
                        LinkedTreeMap<String, Object> map = new LinkedTreeMap<String, Object>();
                        map.put("subtitles", list.get(0));
                        result.result = map;
                    }
                    callback.onCompleted(e, result);
                }
            });
        }
    }

    public ResponseFuture<RpcResponse> setSubtitle(String subLang, FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("setsubtitle", Arrays.asList(subLang));
        request.id = RequestId.SET_SUBTITLE.ordinal();
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> listenNotifications(Double volume, FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("listennotifications");
        request.id = RequestId.LISTENNOTIFICATIONS.ordinal();
        return request(request, callback);
    }

    private ResponseFuture<RpcResponse> request(RpcRequest rpc, final FutureCallback<RpcResponse> callback) {
        ResponseFuture<RpcResponse> response =
                Ion.with(mContext).load(mUrl)
                .basicAuthentication(mUsername, mPassword)
                .setJsonPojoBody(rpc)
                .as(RpcResponse.class);

        response.setCallback(new FutureCallback<RpcResponse>() {
            @Override
            public void onCompleted(Exception e, RpcResponse result) {
                try {
                    if (result != null && result.result != null) {
                        LinkedTreeMap<String, Object> map = result.getMapResult();
                        if (map.containsKey("popcornVersion")) {
                            mVersion = (String) map.get("popcornVersion");
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    mVersion = "0.0.0";
                }
                callback.onCompleted(e, result);
            }
        });
        return response;
    }

    private void checkUrl() {
        if(!mIpAddress.startsWith("http://") && !mIpAddress.startsWith("https://")) {
            mIpAddress = "http://" + mIpAddress;
        }

        if(mIpAddress.endsWith("/")) {
            mIpAddress = mIpAddress.substring(0, mIpAddress.length() - 2);
        }
    }

    private class RpcRequest {
        public String jsonrpc = "2.0";
        public String method;
        public Object params = new ArrayList();
        public Integer id = 10;

        public RpcRequest(String method) {
            this.method = method;
        }

        public RpcRequest(String method, Object params) {
            this(method);
            this.params = params;
        }

        public RpcRequest(String method, Object params, Integer id) {
            this(method, params);
            this.id = id;
        }
    }

    public class RpcResponse {
        public String jsonrpc;
        public Object result;
        public Integer id;

        public LinkedTreeMap<String, Object> getMapResult() {
            if(result instanceof LinkedTreeMap) {
                return (LinkedTreeMap<String, Object>) result;
            }
            return null;
        }
    }

}
