package com.deployai.controller;

import com.deployai.util.SceneManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

public class SplashController implements Initializable {

    @FXML private WebView splashWebView;
    @FXML private WebView logoView;
    @FXML private Button startButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Fondo animado
        WebEngine bgEngine = splashWebView.getEngine();
        splashWebView.setPageFill(javafx.scene.paint.Color.TRANSPARENT);
        bgEngine.loadContent(getBackgroundHtml());

        // Logo SVG animado
        WebEngine logoEngine = logoView.getEngine();
        logoView.setPageFill(javafx.scene.paint.Color.TRANSPARENT);
        logoEngine.loadContent(getLogoHtml());

        // Botón gradiente
        startButton.setStyle(
            "-fx-background-color: linear-gradient(to right, #A855F7 0%, #22D3EE 100%);" +
            "-fx-text-fill: #080A10;" +
            "-fx-font-weight: bold;" +
            "-fx-font-family: 'JetBrains Mono';" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 14 36 14 36;" +
            "-fx-cursor: hand;" +
            "-fx-border-width: 0;"
        );
    }

    @FXML
    private void handleStart() {
        try {
            SceneManager.getInstance().showLogin();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getLogoHtml() {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>" +
        "* { margin:0; padding:0; box-sizing:border-box; }" +
        "body { background:transparent; display:flex; align-items:center; justify-content:center; height:100vh; overflow:hidden; }" +
        ".ring-outer { animation:rotSlow 18s linear infinite; transform-origin:75px 75px; }" +
        ".ring-inner { animation:rotSlowR 12s linear infinite; transform-origin:75px 75px; }" +
        ".core-pulse { animation:breathe 4s ease-in-out infinite; }" +
        "@keyframes rotSlow  { to{transform:rotate(360deg);}  }" +
        "@keyframes rotSlowR { to{transform:rotate(-360deg);} }" +
        "@keyframes breathe  { 0%,100%{opacity:.2} 50%{opacity:.8} }" +
        "</style></head><body>" +
        "<svg width='150' height='150' viewBox='-15 -15 180 180' fill='none' style='overflow:visible'>" +
        "  <circle class='core-pulse' cx='75' cy='75' r='72' fill='none' stroke='#A855F7' stroke-width='1' opacity='.2'/>" +
        "  <g class='ring-outer'>" +
        "    <polygon points='75,8 133,41 133,109 75,142 17,109 17,41' fill='none' stroke='#22D3EE' stroke-width='1.5' opacity='.75'/>" +
        "    <circle cx='75'  cy='8'   r='3.5' fill='#22D3EE'/>" +
        "    <circle cx='133' cy='41'  r='3.5' fill='#22D3EE'/>" +
        "    <circle cx='133' cy='109' r='3.5' fill='#22D3EE'/>" +
        "    <circle cx='75'  cy='142' r='3.5' fill='#22D3EE'/>" +
        "    <circle cx='17'  cy='109' r='3.5' fill='#22D3EE'/>" +
        "    <circle cx='17'  cy='41'  r='3.5' fill='#22D3EE'/>" +
        "  </g>" +
        "  <g class='ring-inner'>" +
        "    <polygon points='75,28 112,49 112,101 75,122 38,101 38,49' fill='none' stroke='#A855F7' stroke-width='1' stroke-dasharray='4 5' opacity='.55'/>" +
        "  </g>" +
        "  <circle cx='75' cy='75' r='28' fill='#0E1018' stroke='#FDE047' stroke-width='2'/>" +
        "  <path d='M64 61 L64 89 L75 89 C86 89 93 82 93 75 C93 68 86 61 75 61 Z' fill='none' stroke='#FDE047' stroke-width='2.8' stroke-linecap='round' stroke-linejoin='round'/>" +
        "  <line x1='64' y1='61' x2='64' y2='89' stroke='#FDE047' stroke-width='2.8' stroke-linecap='round'/>" +
        "  <circle cx='75' cy='75' r='3.5' fill='#22D3EE'/>" +
        "</svg>" +
        "</body></html>";
    }

    private String getBackgroundHtml() {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>" +
        "* { margin:0; padding:0; box-sizing:border-box; }" +
        "body { background:#080A10; overflow:hidden; width:100vw; height:100vh; }" +
        "canvas { position:fixed; top:0; left:0; width:100%; height:100%; }" +
        "</style></head><body>" +
        "<canvas id='bg'></canvas>" +
        "<script>" +
        "var canvas=document.getElementById('bg'),ctx=canvas.getContext('2d');" +
        "function resize(){canvas.width=window.innerWidth;canvas.height=window.innerHeight;}" +
        "resize();window.addEventListener('resize',resize);" +
        "var SYMBOLS='01</>{}[]();=+*&|=>constletfn'.split('');" +
        "var streams=[];" +
        "for(var i=0;i<20;i++){" +
        "  streams.push({" +
        "    x:Math.random()*window.innerWidth," +
        "    y:Math.random()*-600," +
        "    speed:0.3+Math.random()*0.25," +
        "    chars:Array.from({length:16},function(){return SYMBOLS[Math.floor(Math.random()*SYMBOLS.length)];})," +
        "    timer:0," +
        "    color:[[34,211,238],[168,85,247],[16,185,129]][Math.floor(Math.random()*3)]," +
        "    opacity:0.2+Math.random()*0.2" +
        "  });" +
        "}" +
        "var waves=[" +
        "  {y:0.25,amp:22,freq:0.007,speed:0.004,color:[34,211,238],alpha:0.22}," +
        "  {y:0.50,amp:28,freq:0.005,speed:0.003,color:[168,85,247],alpha:0.18}," +
        "  {y:0.72,amp:18,freq:0.010,speed:0.005,color:[16,185,129],alpha:0.15}," +
        "  {y:0.88,amp:22,freq:0.006,speed:0.002,color:[253,224,71],alpha:0.12}" +
        "];" +
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
        "    ctx.strokeStyle='rgba('+r+','+g+','+b+','+w.alpha+')';ctx.lineWidth=1.6;ctx.stroke();" +
        "    var grad=ctx.createLinearGradient(0,cy-w.amp,0,cy+w.amp*3);" +
        "    grad.addColorStop(0,'rgba('+r+','+g+','+b+','+(w.alpha*0.4)+')');" +
        "    grad.addColorStop(1,'rgba('+r+','+g+','+b+',0)');" +
        "    ctx.beginPath();" +
        "    for(var x=0;x<=canvas.width;x+=2){" +
        "      var y=cy+Math.sin(x*w.freq+t*w.speed*100)*w.amp+Math.sin(x*w.freq*0.5-t*w.speed*60)*w.amp*0.4;" +
        "      x===0?ctx.moveTo(x,y):ctx.lineTo(x,y);" +
        "    }" +
        "    ctx.lineTo(canvas.width,cy+w.amp*4);ctx.lineTo(0,cy+w.amp*4);ctx.closePath();" +
        "    ctx.fillStyle=grad;ctx.fill();" +
        "  });" +
        "  ctx.font='12px monospace';" +
        "  streams.forEach(function(s){" +
        "    s.y+=s.speed;" +
        "    if(s.y>canvas.height+300){s.y=-200-Math.random()*400;s.x=Math.random()*canvas.width;}" +
        "    s.timer++;if(s.timer%25===0){var idx=Math.floor(Math.random()*s.chars.length);s.chars[idx]=SYMBOLS[Math.floor(Math.random()*SYMBOLS.length)];}" +
        "    var r=s.color[0],g=s.color[1],b=s.color[2];" +
        "    s.chars.forEach(function(ch,i){" +
        "      var cy=s.y+i*15,fade=Math.max(0,1-i/s.chars.length),alpha=s.opacity*fade;" +
        "      if(alpha<0.01)return;" +
        "      var bright=i===0?Math.min(alpha*3,0.6):alpha;" +
        "      ctx.fillStyle='rgba('+r+','+g+','+b+','+bright+')';ctx.fillText(ch,s.x,cy);" +
        "    });" +
        "  });" +
        "  t+=0.016; requestAnimationFrame(draw);" +
        "}" +
        "draw();" +
        "document.addEventListener('visibilitychange', function() {" +
        "  if (!document.hidden) draw();" +
        "});" +
        "</script></body></html>";
    }
}
    
