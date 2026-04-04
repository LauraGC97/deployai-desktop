package com.deployai.util;

public class MarkdownRenderer {

    public static String toHtml(String markdown) {
        String body = parseMarkdown(markdown);
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'>" +
               "<style>" + getStyles() + "</style>" +
               "<script>" + getScript() + "</script>" +
               "</head><body class='markdown-body'>" + body + "</body></html>";
    }

    private static String parseMarkdown(String text) {
        if (text == null) return "";

        // Extraer bloques de código ANTES de escapar para no tocarlos
        // Los marcamos con placeholders
        java.util.List<String> codeBlocks = new java.util.ArrayList<>();
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("```(\\w*)\\n([\\s\\S]*?)```");
        java.util.regex.Matcher m = p.matcher(text);
        StringBuffer sb = new StringBuffer();
        int idx = 0;
        while (m.find()) {
            String lang = m.group(1);
            String code = m.group(2)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
            String block =
                "<div class='code-block'>" +
                "<div class='code-bar'><span class='code-lang'>" + lang + "</span>" +
                "<button class='copy-btn' onclick='copyCode(this)'>" +
                "<span class='copy-text'>copiar</span></button></div>" +
                "<pre><code class='lang-" + lang + "'>" + code + "</code></pre></div>";
            codeBlocks.add(block);
            m.appendReplacement(sb, "CODEBLOCK_PLACEHOLDER_" + idx + "_END");
            idx++;
        }
        m.appendTail(sb);
        text = sb.toString();

        // Ahora escapar el resto del texto
        text = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");

        // Restaurar placeholders (no se escaparon porque no tenían & < >)
        for (int i = 0; i < codeBlocks.size(); i++) {
            text = text.replace("CODEBLOCK_PLACEHOLDER_" + i + "_END", codeBlocks.get(i));
        }

        // Código inline
        text = text.replaceAll("`([^`]+)`", "<code class='inline-code'>$1</code>");

        // Negrita
        text = text.replaceAll("\\*\\*([^*]+)\\*\\*", "<strong>$1</strong>");

        // Cursiva — solo si no está dentro de un bloque de código
        text = text.replaceAll("\\*([^*\n]+)\\*", "<em>$1</em>");

        // Headings
        text = text.replaceAll("(?m)^### (.+)$", "<h3>$1</h3>");
        text = text.replaceAll("(?m)^## (.+)$",  "<h2>$1</h2>");
        text = text.replaceAll("(?m)^# (.+)$",   "<h1>$1</h1>");

        // Listas con -
        text = text.replaceAll("(?m)^- (.+)$", "<li>$1</li>");
        text = text.replaceAll("((?:<li>.*</li>\\n?)+)", "<ul>$1</ul>");

        // Listas numeradas
        text = text.replaceAll("(?m)^\\d+\\. (.+)$", "<li>$1</li>");

        // Separador
        text = text.replaceAll("(?m)^---$", "<hr/>");

        // Párrafos
        String[] paragraphs = text.split("\n\n");
        StringBuilder result = new StringBuilder();
        for (String para : paragraphs) {
            String trimmed = para.trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.startsWith("<h") || trimmed.startsWith("<ul") ||
                trimmed.startsWith("<div class='code") || trimmed.startsWith("<hr") ||
                trimmed.startsWith("<li")) {
                result.append(trimmed);
            } else {
                trimmed = trimmed.replace("\n", "<br>");
                result.append("<p>").append(trimmed).append("</p>");
            }
        }

        return result.toString();
    }

    private static String getScript() {
    return
        "function copyCode(btn) {" +
        "  var pre = btn.closest('.code-block').querySelector('code');" +
        "  var text = pre.innerText || pre.textContent;" +
        "  var copyText = btn.querySelector('.copy-text');" +
        "  if (navigator.clipboard) {" +
        "    navigator.clipboard.writeText(text).then(function() {" +
        "      copyText.innerText = 'copiado!';" +
        "      btn.classList.add('copied');" +
        "      setTimeout(function() {" +
        "        copyText.innerText = 'copiar';" +
        "        btn.classList.remove('copied');" +
        "      }, 2000);" +
        "    });" +
        "  } else {" +
        "    var ta = document.createElement('textarea');" +
        "    ta.value = text;" +
        "    document.body.appendChild(ta);" +
        "    ta.select();" +
        "    document.execCommand('copy');" +
        "    document.body.removeChild(ta);" +
        "    copyText.innerText = 'copiado!';" +
        "    btn.classList.add('copied');" +
        "    setTimeout(function() {" +
        "      copyText.innerText = 'copiar';" +
        "      btn.classList.remove('copied');" +
        "    }, 2000);" +
        "  }" +
        "}" +
        "function highlight() {" +
        "  document.querySelectorAll('pre code').forEach(function(block) {" +
        "    var code = block.innerHTML;" +
        "    var kwList = ['const','let','var','function','return','if','else','for','while'," +
        "      'class','import','export','from','new','this','try','catch','throw','async','await'," +
        "      'public','private','protected','static','void','int','long','double','float'," +
        "      'String','boolean','true','false','null','undefined','SELECT','FROM','WHERE'," +
        "      'JOIN','INSERT','UPDATE','DELETE','CREATE','TABLE','AND','OR','ON','AS','BY'," +
        "      'ORDER','GROUP','HAVING','LIMIT','INTO','VALUES','SET','DROP','ALTER','INDEX'];" +
        "    kwList.forEach(function(kw) {" +
        "      var re = new RegExp('(^|[^a-zA-Z0-9_])(' + kw + ')([^a-zA-Z0-9_]|$)', 'g');" +
        "      try { code = code.replace(re, '$1<span class=\"kw\">$2</span>$3'); } catch(e) {}" +
        "    });" +
        "    block.innerHTML = code;" +
        "  });" +
        "}" +
        "window.onload = highlight;";
}

    private static String getStyles() {
        return
            "* { margin: 0; padding: 0; box-sizing: border-box; }" +
            "body {" +
            "  font-family: 'JetBrains Mono', monospace;" +
            "  font-size: 13px;" +
            "  line-height: 1.75;" +
            "  color: #F0F9FF;" +
            "  background: transparent;" +
            "  padding: 2px 4px;" +
            "  overflow-x: hidden;" +
            "}" +
            "p { margin-bottom: 12px; color: #F0F9FF; }" +
            "p:last-child { margin-bottom: 0; }" +
            "strong { color: #F0F9FF; font-weight: 700; }" +
            "em { color: #A855F7; font-style: italic; }" +
            "h1 { font-size: 18px; color: #22D3EE; margin: 16px 0 8px; font-weight: 700; }" +
            "h2 { font-size: 15px; color: #A855F7; margin: 14px 0 6px; font-weight: 700; }" +
            "h3 { font-size: 13px; color: #FDE047; margin: 12px 0 4px; font-weight: 700; }" +
            "hr { border: none; border-top: 1px solid rgba(255,255,255,0.06); margin: 16px 0; }" +
            "ul { padding-left: 20px; margin-bottom: 12px; }" +
            "ol { padding-left: 20px; margin-bottom: 12px; }" +
            "li { margin-bottom: 4px; color: #F0F9FF; }" +
            ".inline-code {" +
            "  background: rgba(34,211,238,0.08);" +
            "  border: 1px solid rgba(34,211,238,0.15);" +
            "  border-radius: 4px;" +
            "  padding: 1px 6px;" +
            "  color: #22D3EE;" +
            "  font-size: 12px;" +
            "}" +
            ".code-block {" +
            "  background: #070810;" +
            "  border: 1px solid rgba(255,255,255,0.06);" +
            "  border-radius: 10px;" +
            "  overflow: hidden;" +
            "  margin: 12px 0;" +
            "}" +
            ".code-bar {" +
            "  display: flex;" +
            "  align-items: center;" +
            "  justify-content: space-between;" +
            "  padding: 8px 16px;" +
            "  background: rgba(255,255,255,0.02);" +
            "  border-bottom: 1px solid rgba(255,255,255,0.05);" +
            "}" +
            ".code-lang {" +
            "  font-size: 11px;" +
            "  color: #6B7280;" +
            "  letter-spacing: 1px;" +
            "  text-transform: uppercase;" +
            "}" +
            ".copy-btn {" +
            "  display: flex;" +
            "  align-items: center;" +
            "  gap: 5px;" +
            "  background: rgba(255,255,255,0.04);" +
            "  border: 1px solid rgba(255,255,255,0.08);" +
            "  border-radius: 6px;" +
            "  padding: 4px 10px;" +
            "  color: #6B7280;" +
            "  font-family: 'JetBrains Mono', monospace;" +
            "  font-size: 11px;" +
            "  cursor: pointer;" +
            "}" +
            ".copy-btn:hover {" +
            "  background: rgba(34,211,238,0.08);" +
            "  color: #22D3EE;" +
            "  border-color: rgba(34,211,238,0.2);" +
            "}" +
            ".copy-btn.copied {" +
            "  background: rgba(16,185,129,0.08);" +
            "  color: #10B981;" +
            "  border-color: rgba(16,185,129,0.2);" +
            "}" +
            "pre { padding: 16px 20px; overflow-x: auto; margin: 0; }" +
            "pre code {" +
            "  color: #F0F9FF;" +
            "  font-family: 'JetBrains Mono', monospace;" +
            "  font-size: 13px;" +
            "  line-height: 1.7;" +
            "  white-space: pre;" +
            "}" +
            ".kw { color: #A855F7; font-weight: bold; }" +
            ".str { color: #FDE047; }" +
            ".num { color: #F97316; }" +
            ".cm { color: #4B5563; font-style: italic; }";
    }
}