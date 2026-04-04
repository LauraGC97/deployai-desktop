package com.deployai.controller;

import com.deployai.util.SceneManager;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.net.URL;
import java.util.ResourceBundle;

public class SplashController implements Initializable {

    @FXML private WebView splashWebView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        WebEngine engine = splashWebView.getEngine();
        splashWebView.setPageFill(javafx.scene.paint.Color.TRANSPARENT);

        // Interceptar alert() desde JavaScript para comunicarse con Java
        engine.setOnAlert(event -> {
            if ("goto:login".equals(event.getData())) {
                Platform.runLater(() -> {
                    try {
                        SceneManager.getInstance().showLogin();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });

        // Bridge JS como respaldo
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaApp", new JavaBridge());
            }
        });

        engine.loadContent(getSplashHtml());
    }

    public class JavaBridge {
        public void goToLogin() {
            Platform.runLater(() -> {
                try {
                    SceneManager.getInstance().showLogin();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private String getSplashHtml() {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>" +
        "* { margin:0; padding:0; box-sizing:border-box; }" +
        "body {" +
        "  background: #080A10;" +
        "  display: flex;" +
        "  flex-direction: column;" +
        "  align-items: center;" +
        "  justify-content: center;" +
        "  height: 100vh;" +
        "  font-family: 'JetBrains Mono', monospace;" +
        "  overflow: hidden;" +
        "  user-select: none;" +
        "}" +
        "canvas { position:fixed; top:0; left:0; pointer-events:none; z-index:0; }" +
        ".container {" +
        "  position: relative; z-index: 1;" +
        "  display: flex; flex-direction: column;" +
        "  align-items: center; gap: 28px;" +
        "  animation: fadeIn 1s ease both;" +
        "}" +
        ".badge {" +
        "  display: flex; align-items: center; gap: 8px;" +
        "  border: 1px solid rgba(168,85,247,0.3);" +
        "  border-radius: 99px; padding: 6px 16px;" +
        "  font-size: 12px; color: #A855F7;" +
        "  animation: fadeIn 1s ease 0.2s both;" +
        "}" +
        ".dot { width:6px; height:6px; border-radius:50%; background:#10B981;" +
        "  box-shadow:0 0 6px #10B981; animation: blink 2s infinite; flex-shrink:0; }" +
        ".logo-wrap {" +
        "  position: relative; width: 160px; height: 160px;" +
        "  display: flex; align-items: center; justify-content: center;" +
        "}" +
        ".logo-glow {" +
        "  position: absolute; width: 200px; height: 200px; border-radius: 50%;" +
        "  background: radial-gradient(circle, rgba(168,85,247,0.2) 0%, transparent 70%);" +
        "  animation: pulse-glow 3s ease-in-out infinite;" +
        "}" +
        ".ring-outer { animation: rotSlow 18s linear infinite; transform-origin: 75px 75px; }" +
        ".ring-inner { animation: rotSlowR 12s linear infinite; transform-origin: 75px 75px; }" +
        ".core-pulse { animation: breathe 4s ease-in-out infinite; }" +
        ".title { font-size: 54px; font-weight: 800; letter-spacing: -2px; line-height:1; animation: fadeIn 1s ease 0.3s both; }" +
        ".t1 { color: #22D3EE; } .t2 { color: #A855F7; } .t3 { color: #F0F9FF; }" +
        ".code-line { font-size: 20px; animation: fadeIn 1s ease 0.4s both; }" +
        ".kw-ret { color: #A855F7; } .kw-str { color: #FDE047; }" +
        ".brace { font-size: 36px; color: #F0F9FF; opacity: .4; animation: fadeIn 1s ease 0.45s both; }" +
        ".sub { font-size: 14px; color: #8B9BB4; text-align: center; max-width: 600px; line-height: 1.75; animation: fadeIn 1s ease 0.5s both; }" +
        ".hl-c { color: #22D3EE; } .hl-v { color: #A855F7; } .hl-g { color: #10B981; }" +
        ".cta {" +
        "  background: linear-gradient(90deg, #A855F7, #22D3EE);" +
        "  color: #080A10; border: none; padding: 14px 36px;" +
        "  border-radius: 10px; font-family: 'JetBrains Mono', monospace;" +
        "  font-weight: 700; font-size: 14px; cursor: pointer;" +
        "  transition: transform 0.15s, box-shadow 0.15s;" +
        "  animation: fadeIn 1s ease 0.65s both;" +
        "}" +
        ".cta:hover { transform: translateY(-2px); box-shadow: 0 8px 28px rgba(34,211,238,0.3); }" +
        "@keyframes rotSlow  { to { transform: rotate(360deg);  } }" +
        "@keyframes rotSlowR { to { transform: rotate(-360deg); } }" +
        "@keyframes breathe  { 0%,100%{opacity:.3} 50%{opacity:.9} }" +
        "@keyframes pulse-glow { 0%,100%{transform:scale(1);opacity:.5} 50%{transform:scale(1.15);opacity:1} }" +
        "@keyframes fadeIn { from{opacity:0;transform:translateY(18px)} to{opacity:1;transform:none} }" +
        "@keyframes blink { 0%,100%{opacity:1} 50%{opacity:.3} }" +
        "</style></head><body>" +
        "<canvas id='bg'></canvas>" +
        "<div class='container'>" +
        "  <div class='badge'><span class='dot'></span> v1.0.0 &bull; Asistente de programaci&oacute;n</div>" +
        "  <div class='logo-wrap'>" +
        "    <div class='logo-glow'></div>" +
        "    <svg width='160' height='160' viewBox='-10 -10 170 170' fill='none' style='overflow:visible'>" +
        "      <circle class='core-pulse' cx='75' cy='75' r='68' fill='none' stroke='#A855F7' stroke-width='1' opacity='.15'/>" +
        "      <g class='ring-outer'>" +
        "        <polygon points='75,8 133,41 133,109 75,142 17,109 17,41' fill='none' stroke='#22D3EE' stroke-width='1.5' opacity='.7'/>" +
        "        <circle cx='75'  cy='8'   r='3' fill='#22D3EE'/>" +
        "        <circle cx='133' cy='41'  r='3' fill='#22D3EE'/>" +
        "        <circle cx='133' cy='109' r='3' fill='#22D3EE'/>" +
        "        <circle cx='75'  cy='142' r='3' fill='#22D3EE'/>" +
        "        <circle cx='17'  cy='109' r='3' fill='#22D3EE'/>" +
        "        <circle cx='17'  cy='41'  r='3' fill='#22D3EE'/>" +
        "      </g>" +
        "      <g class='ring-inner'>" +
        "        <polygon points='75,28 112,49 112,101 75,122 38,101 38,49' fill='none' stroke='#A855F7' stroke-width='1' stroke-dasharray='4 5' opacity='.5'/>" +
        "      </g>" +
        "      <circle cx='75' cy='75' r='26' fill='#0E1018' stroke='#FDE047' stroke-width='1.8'/>" +
        "      <path d='M65 62 L65 88 L75 88 C85 88 91 82 91 75 C91 68 85 62 75 62 Z' fill='none' stroke='#FDE047' stroke-width='2.5' stroke-linecap='round' stroke-linejoin='round'/>" +
        "      <line x1='65' y1='62' x2='65' y2='88' stroke='#FDE047' stroke-width='2.5' stroke-linecap='round'/>" +
        "      <circle cx='75' cy='75' r='3' fill='#22D3EE'/>" +
        "    </svg>" +
        "  </div>" +
        "  <div class='title'><span class='t1'>De</span><span class='t2'>ploy</span><span class='t3'>.AI</span></div>" +
        "  <div class='code-line'><span class='kw-ret'>return</span> <span class='kw-str'>\"El futuro\"</span>;</div>" +
        "  <div class='brace'>}</div>" +
        "  <div class='sub'>La IA que <span class='hl-c'>entiende</span> tu c&oacute;digo, <span class='hl-v'>optimiza</span> tu stack y <span class='hl-g'>despliega</span> sin l&iacute;mites.</div>" +
        "  <button class='cta' onclick='alert(\"goto:login\")'>&lt;/&gt; Abrir Deploy AI</button>" +
        "</div>" +
        "<script>" +
        "var canvas = document.getElementById('bg');" +
        "var ctx = canvas.getContext('2d');" +
        "function resize() { canvas.width = window.innerWidth; canvas.height = window.innerHeight; }" +
        "resize(); window.addEventListener('resize', resize);" +
        "var SYMBOLS = '01</>{}[]();=+*&|=>constletfn'.split('');" +
        "var streams = [];" +
        "for (var i = 0; i < 28; i++) {" +
        "  streams.push({" +
        "    x: Math.random()*window.innerWidth," +
        "    y: Math.random()*-600," +
        "    speed: 0.3+Math.random()*0.5," +
        "    chars: Array.from({length:14},function(){return SYMBOLS[Math.floor(Math.random()*SYMBOLS.length)];})," +
        "    timer: 0," +
        "    color: [[34,211,238],[168,85,247],[16,185,129]][Math.floor(Math.random()*3)]," +
        "    opacity: 0.1+Math.random()*0.12" +
        "  });" +
        "}" +
        "var waves=[{y:0.25,amp:22,freq:0.007,speed:0.004,color:[34,211,238],alpha:0.12}," +
        "  {y:0.50,amp:28,freq:0.005,speed:0.003,color:[168,85,247],alpha:0.10}," +
        "  {y:0.72,amp:18,freq:0.010,speed:0.005,color:[16,185,129],alpha:0.08}," +
        "  {y:0.88,amp:22,freq:0.006,speed:0.002,color:[253,224,71],alpha:0.06}];" +
        "var t=0;" +
        "function draw(){" +
        "  ctx.clearRect(0,0,canvas.width,canvas.height);" +
        "  waves.forEach(function(w){" +
        "    var r=w.color[0],g=w.color[1],b=w.color[2],cy=canvas.height*w.y;" +
        "    ctx.beginPath();" +
        "    for(var x=0;x<=canvas.width;x+=2){" +
        "      var y=cy+Math.sin(x*w.freq+t*w.speed*100)*w.amp+Math.sin(x*w.freq*0.5-t*w.speed*60)*w.amp*0.4;" +
        "      x===0?ctx.moveTo(x,y):ctx.lineTo(x,y);" +
        "    }" +
        "    ctx.strokeStyle='rgba('+r+','+g+','+b+','+w.alpha+')';ctx.lineWidth=1.4;ctx.stroke();" +
        "  });" +
        "  ctx.font='11px monospace';" +
        "  streams.forEach(function(s){" +
        "    s.y+=s.speed;" +
        "    if(s.y>canvas.height+300){s.y=-200-Math.random()*400;s.x=Math.random()*canvas.width;}" +
        "    s.timer++;if(s.timer%25===0){var idx=Math.floor(Math.random()*s.chars.length);s.chars[idx]=SYMBOLS[Math.floor(Math.random()*SYMBOLS.length)];}" +
        "    var r=s.color[0],g=s.color[1],b=s.color[2];" +
        "    s.chars.forEach(function(ch,i){" +
        "      var cy=s.y+i*15,fade=Math.max(0,1-i/s.chars.length),alpha=s.opacity*fade;" +
        "      if(alpha<0.008)return;" +
        "      ctx.fillStyle='rgba('+r+','+g+','+b+','+alpha+')';ctx.fillText(ch,s.x,cy);" +
        "    });" +
        "  });" +
        "  t+=0.016;requestAnimationFrame(draw);" +
        "}" +
        "draw();" +
        "</script></body></html>";
    }
}
