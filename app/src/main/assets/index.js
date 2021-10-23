//window.index = {
     function sayHi(){
        var element1 = document.getElementById("input1");
        element1.style.height = "150px";
        element1.style.background = "green";
        this.f2();
    }
   function  f2(){
        var ip1Ele = document.getElementsByClassName("inp1")[0];
        ip1Ele.style.color = "white";
        ip1Ele.style.fontSize = "35px";
    }
      function getUserInfo(callback) {
        var msgBody = {};
        msgBody.handler = 'XCUserInfo';//native 提供的模块
        msgBody.action = 'getUserInfo';//native 提供的方法
        msgBody.params = { "platformCode": "001" }; //平台code参数
        this.sendMessage(msgBody, function (data) {
          callback(data);
        })
      }
        function ua() {
          return navigator.userAgent;
        }
        function isAndroid() {
          return (/(Android);?[\s\/]+([\d.]+)?/.test(this.ua));
        }
        function isIOS() {
          return !!this.ua.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/);
        }
        msgCallbackMap: {}
        eventCallMap: {}
        function sendMessage (data, callback) {
          if (callback && typeof (callback) === 'function') {
            var params = data.params;
            var callbackid = Date.parse(new Date()) + Math.random().toString(16).substr(3);//通过时间戳+随机数生成 ,当前仅时间戳
            this.msgCallbackMap[callbackid] = callback;
            params.callbackID = callbackid;
            params.callbackFunction = 'window.XCZX.callbackDispatcher';
          }
          if (this.isAndroid) {
            try {
              prompt(JSON.stringify([data]));
            }
            catch (error) {
              console.log('error native message');
            }
          }
        }
//}