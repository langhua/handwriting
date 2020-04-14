## 前端开发笔记

#### 前端选择MyScript的原因
基于以下两个原因：
1. MyScript有完整的前端开源方案，从安卓、iOS、Windows到Web，Web支持REST/HTTP和WebSockets协议，支持npm和node.js，在React、Angular、Vue等流行框架下，均可使用。
2. 具有中文字手写识别能力，弥补了杜彪汉字检查库不能识别手写汉字的不足。

下面以Javascript和Vue为例，说明如何让MyScript前端与汉字手写检查后台对接。

#### web-samples
对MyScript的web-integration-samples的web-samples修改，尽在新的[index.html]( https://github.com/langhua/web-integration-samples/blob/master/web-samples/index.html)中。

其中的要点是编辑器事件监听，要写在配置(configuration)之前。

```
<script type="text/javascript">
    textInput.addEventListener('exported', function(event) {
        ...
    });

    textInput.configuration = {
        ...
    };
</script>
```

另外，由于在使用SVG输出汉字正确写法时，使用了animejs，需要在index.html中，加入：

```
<script src="./node_modules/animejs/lib/anime.min.js"></script>
```

并按下面的构建次序来运行：

```
npm install
npm install animejs --save
bower install
npm run-script run
```

#### vue-integration-examples
对MyScript的web-integration-samples的vue-integration-examples修改，尽在新的[myscriptjs-vue-component.vue]( https://github.com/langhua/web-integration-samples/blob/master/vue-integration-examples/src/components/myscriptjs-vue-component.vue)中。

与Javascript中一样，编辑器事件监听，要写在配置(configuration)之前。

在使用SVG输出汉字正确写法时，使用了animejs，需要在myscriptjs-vue-component.vue中，加入：

```
import anime from 'animejs';
```

并按下面的构建次序来运行：

```
npm install
npm install animejs --save
npm run dev
```

#### 与汉字手写检查后台对接
与汉字手写检查后台对接是通过getStrokeEvalResult(han, myscriptStrokes)函数中的fetch语句实现的，当前的实现方法没有跨域问题。

