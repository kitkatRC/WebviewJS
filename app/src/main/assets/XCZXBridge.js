window.XCZX = {
  get ua() {
    return navigator.userAgent;
  },
  get isAndroid() {
    return (/(Android);?[\s\/]+([\d.]+)?/.test(this.ua));
  },
  get isIOS() {
    return !!this.ua.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/);
  },
  msgCallbackMap: {},
  eventCallMap: {},
  sendMessage: function (data, callback) {
    if (callback && typeof (callback) === 'function') {
      var params = data.params;
      var callbackid = Date.parse(new Date()) + Math.random().toString(16).substr(3);//通过时间戳+随机数生成 ,当前仅时间戳
      this.msgCallbackMap[callbackid] = callback;
      params.callbackID = callbackid;
      params.callbackFunction = 'window.XCZX.callbackDispatcher';
    }
    if (this.isIOS) {
      try {
        window.webkit.messageHandlers.XCZXWKJSBridge.postMessage(data);
      }
      catch (error) {
        console.log('error native message');
      }

    }
    if (this.isAndroid) {
      try {
        prompt(JSON.stringify([data]));
      }
      catch (error) {
        console.log('error native message');
      }
    }
  },
  callbackDispatcher: function (callbackId, resultjson) {
    var handler = this.msgCallbackMap[callbackId];
    if (handler && typeof (handler) === 'function') {
      // JSON.parse(resultjson)
      console.log(resultjson);
      var resultObj = resultjson ? JSON.parse(resultjson) : {};
      handler(resultObj);
    }
  },
  onListenEvent: function (eventId, handler) {
    var handlerArr = this.eventCallMap[eventId];
    if (handlerArr === undefined) {
      handlerArr = [];
      this.eventCallMap[eventId] = handlerArr;
    }
    if (handler !== undefined) {
      handlerArr.push(handler);
    }
  },
  eventDispatcher: function (eventId, resultjson) {
    var handlerArr = this.eventCallMap[eventId];
    for (var key in handlerArr) {
      if (handlerArr.hasOwnProperty(key)) {
        var handler = handlerArr[key];
        if (handler && typeof (handler) === 'function') {
          var resultObj = resultjson ? JSON.parse(resultjson) : {};
          handler(resultObj);
        }
      }
    }
  },
  getUserInfo: function (callback) {
    var msgBody = {};
    msgBody.handler = 'XCUserInfo';//native 提供的模块
    msgBody.action = 'getUserInfo';//native 提供的方法
    msgBody.params = { "platformCode": "001" }; //平台code参数
    this.sendMessage(msgBody, function (data) {
      callback(data);
    })
  }
};