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
import eu.se_bastiaan.popcorntimeremote.utils.LogUtils;
import eu.se_bastiaan.popcorntimeremote.utils.Version;

public class PopcornTimeRpcClient {

    private Context mContext;
    private String mIpAddress, mPort, mUrl, mUsername, mPassword, mVersion;

    public enum RequestId { PING, UP, DOWN, LEFT, RIGHT, ENTER, BACK, QUALITY, NEXT_SEASON, PREV_SEASON, TOGGLE_PLAY, TOGGLE_TABS, TOGGLE_FULLSCREEN, TOGGLE_FAVOURITE, TOGGLE_WATCHED, TOGGLE_MUTE, SET_VOLUME, GET_VOLUME, GET_PLAYING, FILTER_GENRE, FILTER_SORTER, FILTER_TYPE, FILTER_SEARCH, CLEAR_SEARCH, SEEK, GET_VIEWSTACK, SET_SELECTION, GET_SELECTION, GET_FULLSCREEN, GET_SUBTITLES, GET_PLAYERS, SET_PLAYER, SET_SUBTITLE, LISTENNOTIFICATIONS }

    public PopcornTimeRpcClient(Context context, String ipAddress, String port, String username, String password, String version) {
        mVersion = version;
        init(context, ipAddress, port, username, password);
    }

    public PopcornTimeRpcClient(Context context, String ipAddress, String port, String username, String password) {
        mVersion = "0.0.0";
        init(context, ipAddress, port, username, password);
    }

    private void init(Context context, String ipAddress, String port, String username, String password) {
        mContext = context;
        mIpAddress = ipAddress;
        mPort = port;
        mUsername = username;
        mPassword = password;

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
        RpcRequest request  = new RpcRequest("ping", RequestId.PING);
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> up(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("up", RequestId.UP);
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> down(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("down", RequestId.DOWN);
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> left(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("left", RequestId.LEFT);
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> right(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("right", RequestId.RIGHT);
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> enter(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("enter", RequestId.ENTER);
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> back(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("back", RequestId.BACK);
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> toggleQuality(FutureCallback<RpcResponse> callback) {
        RpcRequest request;
        if(Version.compare(mVersion, "0.0.0")) {
            request = new RpcRequest("togglequality", RequestId.QUALITY);
        } else {
            request = new RpcRequest("quality", RequestId.QUALITY);
        }
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> nextSeason(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("nextseason", RequestId.NEXT_SEASON);
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> prevSeason(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("previousseason", RequestId.PREV_SEASON);
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> togglePlay(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("toggleplaying", RequestId.TOGGLE_PLAY);
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> toggleTabs(FutureCallback<RpcResponse> callback) {
        RpcRequest request;
        if(Version.compare(mVersion, "0.0.0")) {
            request = new RpcRequest("toggletab", RequestId.TOGGLE_TABS);
        } else {
            request = new RpcRequest("togglemoviesshows", RequestId.TOGGLE_TABS);
        }
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> toggleFullscreen(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("togglefullscreen", RequestId.TOGGLE_FULLSCREEN);
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> toggleFavourite(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("togglefavourite", RequestId.TOGGLE_FAVOURITE);
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> toggleWatched(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("togglewatched", RequestId.TOGGLE_WATCHED);
        return request(request, callback);
    }

    /**
     * Toggle volume mute
     * @param callback Callback for the request
     * @return ResponseFuture
     */
    public ResponseFuture<RpcResponse> toggleMute(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("togglemute", RequestId.TOGGLE_MUTE);
        return request(request, callback);
    }

    /**
     * Set volume of video player
     * @param volume Volume to set
     * @param callback Callback for the request
     * @return ResponseFuture
     */
    public ResponseFuture<RpcResponse> setVolume(Double volume, FutureCallback<RpcResponse> callback) {
        RpcRequest request;
        if(Version.compare(mVersion, "0.0.0")) {
            request = new RpcRequest("volume", Arrays.asList(volume), RequestId.SET_VOLUME);
        } else {
            request = new RpcRequest("setvolume", Arrays.asList(volume), RequestId.SET_VOLUME);
        }
        return request(request, callback);
    }

    /**
     * Get volume of player
     * @param callback Callback for the request
     * @return ResponseFuture
     */
    public ResponseFuture<RpcResponse> getVolume(FutureCallback<RpcResponse> callback) {
        RpcRequest request;
        if(Version.compare(mVersion, "0.0.0")) {
            request = new RpcRequest("volume", RequestId.GET_VOLUME);
        } else {
            RpcResponse response = new RpcResponse();
            LinkedTreeMap<String, Object> map = new LinkedTreeMap<String, Object>();
            map.put("volume", 1);
            response.result = map;
            callback.onCompleted(null, response);
            return null;
        }
        return request(request, callback);
    }

    /**
     * Filter by genre
     * @param genre Genre to filter by
     * @param callback Callback for the request
     * @return ResponseFuture
     */
    public ResponseFuture<RpcResponse> filterGenre(String genre, FutureCallback<RpcResponse> callback) {
        RpcRequest request = new RpcRequest("filtergenre", Arrays.asList(genre), RequestId.FILTER_GENRE);
        return request(request, callback);
    }

    /**
     * Sort items by sorter
     * @param sorter Type to sort by
     * @param callback Callback for the request
     * @return ResponseFuture
     */
    public ResponseFuture<RpcResponse> filterSorter(String sorter, FutureCallback<RpcResponse> callback) {
        RpcRequest request = new RpcRequest("filtersorter", Arrays.asList(sorter), RequestId.FILTER_SORTER);
        return request(request, callback);
    }

    /**
     * Filter by type
     * @param type Type to select
     * @param callback Callback for the request
     * @return ResponseFuture
     */
    public ResponseFuture<RpcResponse> filterType(String type, FutureCallback<RpcResponse> callback) {
        RpcRequest request = new RpcRequest("filtertype", Arrays.asList(type), RequestId.FILTER_TYPE);
        return request(request, callback);
    }

    /**
     * Filter items by keywords
     * @param keywords Keywords to search by
     * @param callback Callback for the request
     * @return ResponseFuture
     */
    public ResponseFuture<RpcResponse> filterSearch(String keywords, FutureCallback<RpcResponse> callback) {
        RpcRequest request;
        request = new RpcRequest("filtersearch", Arrays.asList(keywords), RequestId.FILTER_SEARCH);
        return request(request, callback);
    }

    /**
     * Clear the search input
     * @param callback Callback for the request
     * @return ResponseFuture
     */
    public ResponseFuture<RpcResponse> clearSearch(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("clearsearch", RequestId.CLEAR_SEARCH);
        return request(request, callback);
    }

    /**
     * Seek {seconds} in the video
     * @param seconds Seconds to seek forward or backward
     * @param callback Callback for the request
     * @return ResponseFuture
     */
    public ResponseFuture<RpcResponse> seek(Integer seconds, FutureCallback<RpcResponse> callback) {
        RpcRequest request;
        request = new RpcRequest("seek", Arrays.asList(seconds));
        request.id = RequestId.SEEK.ordinal();
        return request(request, callback);
    }

    /**
     * Get current viewstack of the application
     * @param callback Callback for the request
     * @return ResponseFuture
     */
    public ResponseFuture<RpcResponse> getViewstack(final FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("getviewstack", RequestId.GET_VIEWSTACK);
        if(Version.compare(mVersion, "0.0.0")) {
            return request(request, callback);
        } else {
            return request(request, new FutureCallback<RpcResponse>() {
                @Override
                public void onCompleted(Exception e, RpcResponse result) {
                    try {
                        if (e == null && result != null && result.result != null) {
                            ArrayList list = (ArrayList) result.result;
                            LinkedTreeMap<String, Object> map = new LinkedTreeMap<String, Object>();
                            map.put("viewstack", list.get(0));
                            result.result = map;
                        }
                        callback.onCompleted(e, result);
                    } catch(Exception exception) {
                        exception.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * Get available subtitles for the current playing or selected video
     * @param callback Callback for the request
     * @return ResponseFuture
     */
    public ResponseFuture<RpcResponse> getSubtitles(final FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("getsubtitles", RequestId.GET_SUBTITLES);
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

    /**
     * Get selection in the list browser or the opened detail
     * @param callback Callback for the request
     * @return ResponseFuture
     */
    public ResponseFuture<RpcResponse> getSelection(final FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("getselection", RequestId.GET_SELECTION);
        if(Version.compare(mVersion, "0.0.0")) {
            return request(request, callback);
        } else {
            callback.onCompleted(new UnsupportedOperationException("Old API, method not implemented"), null);
            return null;
        }
    }

    /**
     * Set selection in the list browser
     * @param index Index of the item that has to become selected
     * @param callback Callback for the request
     * @return ResponseFuture
     */
    public ResponseFuture<RpcResponse> setSelection(Integer index, final FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("setselection", Arrays.asList(index), RequestId.SET_SELECTION);
        if(Version.compare(mVersion, "0.3.4")) {
            return request(request, callback);
        } else {
            callback.onCompleted(new UnsupportedOperationException("Old API, method not implemented"), null);
            return null;
        }
    }

    /**
     * Get the current playing video information
     * @param callback Callback for the request
     * @return ResponseFuture
     */
    public ResponseFuture<RpcResponse> getPlaying(final FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("getplaying", RequestId.GET_PLAYING);
        if(Version.compare(mVersion, "0.0.0")) {
            return request(request, callback);
        } else {
            callback.onCompleted(new UnsupportedOperationException("Old API, method not implemented"), null);
            return null;
        }
    }

    /**
     * Get the current fullscreen status of the application
     * @param callback Callback for the request
     * @return ResponseFuture
     */
    public ResponseFuture<RpcResponse> getFullscreen(final FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("getfullscreen", RequestId.GET_FULLSCREEN);
        if(Version.compare(mVersion, "0.0.0")) {
            return request(request, callback);
        } else {
            callback.onCompleted(new UnsupportedOperationException("Old API, method not implemented"), null);
            return null;
        }
    }

    /**
     * Get available media players for the instance
     * @param callback Callback for the request
     * @return ResponseFuture
     */
    public ResponseFuture<RpcResponse> getPlayers(final FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("getplayers", RequestId.GET_PLAYERS);
        if(Version.compare(mVersion, "0.0.0")) {
            return request(request, callback);
        } else {
            callback.onCompleted(new UnsupportedOperationException("Old API, method not implemented"), null);
            return null;
        }
    }

    /**
     * Set the media player of instance
     * @param playerId New id of the player
     * @param callback Callback for the request
     * @return ResponseFuture
     */
    public ResponseFuture<RpcResponse> setPlayer(String playerId, FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("setplayer", Arrays.asList(playerId), RequestId.SET_PLAYER);
        return request(request, callback);
    }

    /**
     * Set subtitle language of instance
     * @param subLang New language for subtitles
     * @param callback Callback for the request
     * @return ResponseFuture
     */
    public ResponseFuture<RpcResponse> setSubtitle(String subLang, FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("setsubtitle", Arrays.asList(subLang), RequestId.SET_SUBTITLE);
        return request(request, callback);
    }

    public ResponseFuture<RpcResponse> listenNotifications(FutureCallback<RpcResponse> callback) {
        RpcRequest request  = new RpcRequest("listennotifications", RequestId.LISTENNOTIFICATIONS);
        return request(request, callback);
    }

    /**
     * Send JSON RPC request to the instance
     * @param rpc Request data
     * @param callback Callback for the request
     * @return ResponseFuture
     */
    private ResponseFuture<RpcResponse> request(final RpcRequest rpc, final FutureCallback<RpcResponse> callback) {
        if(mContext == null) return null;

        ResponseFuture<RpcResponse> response =
                Ion.with(mContext.getApplicationContext()).load(mUrl)
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
                    LogUtils.d("Version", "Exception: " + mVersion);
                    if(rpc.id == RequestId.GET_SELECTION.ordinal()) {
                        mVersion = "0.3.4";
                    }
                }
                callback.onCompleted(e, result);
            }
        });
        return response;
    }

    /**
     * Check the ip address to start with http//
     */
    private void checkUrl() {
        if(!mIpAddress.startsWith("http://") && !mIpAddress.startsWith("https://")) {
            mIpAddress = "http://" + mIpAddress;
        }

        if(mIpAddress.endsWith("/")) {
            mIpAddress = mIpAddress.substring(0, mIpAddress.length() - 2);
        }
    }

    private class RpcRequest {
        final public String jsonrpc = "2.0";
        final public String method;
        public Object params = new ArrayList();
        public Integer id = 10;

        public RpcRequest(String method) {
            this.method = method;
        }

        public RpcRequest(String method, Object params) {
            this(method);
            this.params = params;
        }

        public RpcRequest(String method, Object params, RequestId id) {
            this(method, params);
            this.id = id.ordinal();
        }

        public RpcRequest(String method, RequestId id) {
            this(method);
            this.id = id.ordinal();
        }
    }

    public class RpcResponse {
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