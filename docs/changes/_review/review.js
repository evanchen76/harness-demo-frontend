// review 頁共用程式：各需求的 review.html 只寫內容，呼叫 reviewPage({...}) 畫出頁面。
//
// 機器產生或已經寫在別處的內容，頁面打開時直接讀檔，不要抄進 review.html：
//   check-green.txt、check-red.txt、spec.approved  → 測試數、逐案結果、實作前的紅燈、hash 對照
//   requirement.md                          → 完成標準、決策、不做的事
//   config.migrations 列的 db/migration 檔   → SQL 全文
//
// reviewPage(config)：
//   slug, title, testClass（這個需求的測試類別名稱，用來對照完成標準的測案）
//   summary: [一段一句的文字]
//   attention: [{ kind, title, body }]      AI 另外要人看的事（kind：核准後變動／沒辦法驗／只有自動測試／其他）
//   groups: [{ title, note, steps: [...] }] 親手試的步驟
//   apiChanges: [{ endpoint, change }]      既有 API 的回應或行為變了什麼
//   changes: [{ title, files: [{ path, note }] }]
//   migrations: ['V2__xxx.sql']
//   deploy: [文字]                           上線要注意
//   findings: [文字]                         範圍外發現、沒動手的事（沒有就空陣列）
//
// 步驟：{ title, why, covers: ['D1', ...], method, path, body（物件或省略）,
//         expect: [{ status, code }]（符合其中一組就算對）,
//         checks: [{ label, test: (回應 JSON) => true/false }] }
// body、path 裡的 {{rand}} 每次送出時換成一段隨機碼（給不能重複的 sku 用）。
(function () {
  'use strict';

  function el(tag, cls, text) {
    const e = document.createElement(tag);
    if (cls) e.className = cls;
    if (text !== undefined) e.textContent = text;
    return e;
  }

  // 文字裡的 `code` 畫成 <code>，其餘照原文
  function rich(parent, text) {
    String(text).split(/(`[^`]+`)/).forEach(part => {
      if (part.startsWith('`') && part.endsWith('`') && part.length > 2) parent.appendChild(el('code', '', part.slice(1, -1)));
      else if (part) parent.appendChild(document.createTextNode(part));
    });
    return parent;
  }

  function rand() {
    return Math.random().toString(36).slice(2, 8).toUpperCase();
  }

  async function load(name) {
    try {
      const res = await fetch(name, { cache: 'no-store' });
      return res.ok ? await res.text() : null;
    } catch (e) {
      return null;
    }
  }

  // check-red.txt、check-green.txt、spec.approved：開頭是 key: value，之後一個測試一行
  function parseCheck(text) {
    if (!text) return null;
    const head = {};
    const tests = [];
    text.split('\n').forEach(line => {
      const t = line.match(/^(PASS|FAIL|ERROR|SKIP\w*)\s+(\S+)\.(\S+)\s*$/);
      if (t) { tests.push({ result: t[1], cls: t[2], method: t[3] }); return; }
      const h = line.match(/^([a-z_]+):\s*([^#]*)/);
      if (h && !(h[1] in head)) head[h[1]] = h[2].trim();
    });
    return { head, tests };
  }

  // requirement.md 裡某個 ## 標題底下的內容
  function mdSection(md, heading) {
    if (!md) return [];
    const lines = md.split('\n');
    const start = lines.findIndex(l => l.trim() === '## ' + heading);
    if (start < 0) return [];
    const rest = lines.slice(start + 1);
    const end = rest.findIndex(l => l.startsWith('## '));
    return end < 0 ? rest : rest.slice(0, end);
  }

  function mdTable(lines) {
    return lines
      .filter(l => l.trim().startsWith('|'))
      .map(l => l.trim().replace(/^\||\|$/g, '').split('|').map(c => c.trim()))
      .filter(cells => !cells.every(c => /^-+$/.test(c)))
      .slice(1);
  }

  // 「TC01、TC16～TC22」→ [1, 16, 17, ... 22]
  function tcNumbers(text) {
    const out = new Set();
    const re = /TC(\d+)(?:\s*[～~]\s*TC(\d+))?/g;
    let m;
    while ((m = re.exec(text))) {
      const from = Number(m[1]);
      const to = m[2] ? Number(m[2]) : from;
      for (let i = from; i <= to; i++) out.add(i);
    }
    return [...out];
  }

  function section(parent, id, title, note) {
    const sec = el('section', 'sec');
    sec.id = id;
    sec.appendChild(el('h2', '', title));
    sec.firstChild.dataset.no = String(parent.querySelectorAll('section.sec').length + 1);
    if (note) rich(sec.appendChild(el('p', 'note')), note);
    parent.appendChild(sec);
    return sec;
  }

  function table(headers, cls) {
    const wrap = el('div', 'table-wrap');
    const t = el('table', cls || '');
    const tr = el('tr');
    headers.forEach(h => tr.appendChild(el('th', '', h)));
    t.appendChild(el('thead')).appendChild(tr);
    const body = t.appendChild(el('tbody'));
    wrap.appendChild(t);
    return { wrap, body };
  }

  window.reviewPage = async function (config) {
    document.title = config.title + ' review';
    const root = document.getElementById('app');
    const layout = root.appendChild(el('div', 'layout'));
    const nav = layout.appendChild(el('nav', 'nav'));
    nav.appendChild(el('p', 'nav-title', '目錄'));
    const main = layout.appendChild(el('main', 'main'));

    const [t5Text, t4Text, r1Text, reqText, ...sqlTexts] = await Promise.all([
      load('check-green.txt'), load('check-red.txt'), load('spec.approved'), load('requirement.md'),
      ...(config.migrations || []).map(f => load('/try/' + f)),
    ]);
    const t5 = parseCheck(t5Text);
    const t4 = parseCheck(t4Text);
    const r1 = parseCheck(r1Text);

    // ---- 頁首 ----
    const head = el('header', 'head');
    head.appendChild(el('p', 'eyebrow', 'review · ' + config.slug));
    head.appendChild(el('h1', '', config.title));
    (config.summary || []).forEach(s => rich(head.appendChild(el('p', 'intro')), s));
    main.appendChild(head);

    function navLink(id, text) {
      const a = el('a', '', text);
      a.href = '#' + id;
      nav.appendChild(a);
    }

    // ---- 1 結論 ----
    navLink('verdict', '結論');
    const verdict = section(main, 'verdict', '結論');
    const tiles = el('div', 'tiles');
    verdict.appendChild(tiles);
    function tile(label, value, state, sub) {
      const t = el('div', 'tile' + (state ? ' ' + state : ''));
      t.appendChild(el('p', 'tile-label', label));
      const v = el('p', 'tile-value', value);
      t.appendChild(v);
      const s = el('p', 'tile-sub', sub || '');
      t.appendChild(s);
      tiles.appendChild(t);
      return { value: v, sub: s, box: t };
    }
    if (t5) {
      tile('自動測試（實作後）', t5.head.result === 'PASS' ? '全部通過' : '有紅燈',
        t5.head.result === 'PASS' ? 'pass' : 'fail', t5.head.total + '；' + t5.head.time);
    } else {
      tile('自動測試（實作後）', '讀不到紀錄', 'fail', 'check-green.txt 不存在');
    }
    if (t4) {
      const red = t4.tests.filter(t => t.cls === config.testClass && t.result !== 'PASS').length;
      const all = t4.tests.filter(t => t.cls === config.testClass).length;
      tile('實作前的紅燈', `${red} / ${all} 個新測試是紅的`, '', t4.head.total);
    }
    const decisionCount = mdTable(mdSection(reqText, '決策')).length;
    tile('需要你判斷的事', `${(config.attention || []).length} 件`, 'warn', `另有 ${decisionCount} 條 AI 的決策`);

    // ---- 2 需要你判斷的事 ----
    navLink('attention', '需要你判斷');
    const att = section(main, 'attention', '需要你判斷的事',
      '規格沒寫到、AI 自己選的做法，以及 AI 沒辦法驗的東西。這一段最值得花時間。');

    // hash 對照：由紀錄算出來，不是 AI 寫的
    const facts = el('ul', 'facts');
    function fact(ok, text) {
      const li = el('li', ok ? 'ok' : 'warn');
      li.appendChild(el('span', 'tick', ok ? '✓' : '!'));
      rich(li.appendChild(el('span')), text);
      facts.appendChild(li);
    }
    if (t4 && t5) {
      fact(t4.head.test_hash === t5.head.test_hash,
        t4.head.test_hash === t5.head.test_hash
          ? '紅燈之後測試程式沒有改過（`check-red.txt` 與 `check-green.txt` 的 test_hash 相同）'
          : '紅燈之後測試程式被改過（test_hash 不同），改了什麼見下方');
      fact(t4.head.spec_hash === t5.head.spec_hash,
        t4.head.spec_hash === t5.head.spec_hash
          ? '實作期間規格沒有變動（spec_hash 相同）'
          : '實作期間規格（測案、OpenAPI、migration）有變動（spec_hash 不同），改了什麼見下方');
    }
    if (r1 && t5) {
      fact(r1.head.spec_hash === t5.head.spec_hash,
        r1.head.spec_hash === t5.head.spec_hash
          ? `你核准的規格跟實作完成時的規格是同一份（規格核准於 ${r1.head.approved_at}）`
          : '你核准的規格跟實作完成時的規格不是同一份，規格需要重新核准');
    } else if (!r1) {
      fact(false, '讀不到 `spec.approved`');
    }
    att.appendChild(facts);

    (config.attention || []).forEach(a => {
      const card = el('article', 'att');
      card.appendChild(el('span', 'kind', a.kind));
      rich(card.appendChild(el('h3')), a.title);
      rich(card.appendChild(el('p')), a.body);
      att.appendChild(card);
    });

    const decisions = mdTable(mdSection(reqText, '決策'));
    if (decisions.length) {
      att.appendChild(el('h3', 'sub', `決策（${decisions.length} 條，來自 requirement.md）`));
      const t = table(['日期', '決策', '理由']);
      decisions.forEach(row => {
        const tr = el('tr');
        row.slice(0, 3).forEach((c, i) => rich(tr.appendChild(el('td', i === 0 ? 'nowrap' : '')), c));
        t.body.appendChild(tr);
      });
      att.appendChild(t.wrap);
    }

    // ---- 3 完成標準 ----
    navLink('criteria', '完成標準');
    const crit = section(main, 'criteria', '完成標準對照',
      '完成標準與對應測案來自 requirement.md，測試結果來自檢查紀錄。點步驟編號跳到親手試的那一步。');
    const critTable = table(['#', '完成標準', '自動測試', '實作前', '親手試']);
    crit.appendChild(critTable.wrap);
    const critRows = {};
    mdTable(mdSection(reqText, '完成標準')).forEach(row => {
      const [id, text, checks] = row;
      const tr = el('tr');
      tr.appendChild(el('td', 'nowrap strong', id));
      rich(tr.appendChild(el('td')), text);
      const nums = tcNumbers(checks || '');
      const autoTd = tr.appendChild(el('td', 'nowrap'));
      const redTd = tr.appendChild(el('td', 'nowrap'));
      if (!nums.length) {
        autoTd.className = '';
        rich(autoTd, checks || '沒有對應測案');
        autoTd.classList.add('warn-text');
        redTd.textContent = '';
      } else {
        const pick = rec => nums.map(n => {
          const prefix = 'tc' + String(n).padStart(2, '0') + '_';
          return rec && rec.tests.find(t => t.cls === config.testClass && t.method.startsWith(prefix));
        });
        const after = pick(t5);
        const passed = after.filter(t => t && t.result === 'PASS').length;
        autoTd.textContent = `${passed} / ${nums.length} 通過`;
        autoTd.classList.add(passed === nums.length ? 'ok-text' : 'bad-text');
        autoTd.title = (checks || '');
        if (t4) {
          const before = pick(t4);
          const red = before.filter(t => t && t.result !== 'PASS').length;
          redTd.textContent = `${red} / ${nums.length} 紅`;
          if (red < nums.length) redTd.classList.add('warn-text');
        }
      }
      const stepTd = tr.appendChild(el('td', 'steps-cell'));
      critRows[id] = stepTd;
      critTable.body.appendChild(tr);
    });

    // ---- 4 親手試 ----
    navLink('try', '親手試');
    const trySec = section(main, 'try', '親手試',
      '步驟有先後關係，請由上往下執行；每一步的路徑和內容都能改了再送。想整個重來：終端機跑 `./dev.sh app-restart`（DB 會回到預設資料）。');
    const bar = el('div', 'bar');
    const runAll = el('button', 'primary', '全部依序執行');
    const clear = el('button', '', '清除結果');
    const opLabel = el('label', 'op', 'X-Operator ');
    const op = el('input');
    op.value = 'reviewer';
    opLabel.appendChild(op);
    const summary = el('span', 'summary', '尚未執行');
    bar.append(runAll, clear, opLabel, summary);
    trySec.appendChild(bar);

    const cards = [];
    let runningAll = false;

    function refreshSummary() {
      const done = cards.filter(c => c.state !== 'idle');
      const bad = done.filter(c => c.state === 'fail').length;
      const state = done.length ? (bad ? 'fail' : 'pass') : '';
      const text = done.length
        ? `已執行 ${done.length} / ${cards.length} 步，${done.length - bad} 步符合預期` + (bad ? `，${bad} 步不符` : '')
        : '尚未執行';
      summary.className = 'summary' + (state ? ' ' + state : '');
      summary.textContent = text;
    }

    let no = 0;
    config.groups.forEach(group => {
      const g = el('div', 'group');
      g.appendChild(el('h3', 'group-title', group.title));
      if (group.note) rich(g.appendChild(el('p', 'note')), group.note);

      group.steps.forEach(step => {
        no += 1;
        const myNo = no;
        const card = el('details', 'card');
        card.id = 'step-' + myNo;
        const top = el('summary', 'card-top');
        top.appendChild(el('span', 'no', String(myNo)));
        const titles = el('div', 'titles');
        const h = el('h4', '', step.title);
        (step.covers || []).forEach(d => h.appendChild(el('span', 'tag', d)));
        titles.appendChild(h);
        const brief = el('p', 'brief');
        brief.appendChild(el('span', 'method ' + step.method.toLowerCase(), step.method));
        brief.appendChild(el('span', 'brief-path', step.path));
        titles.appendChild(brief);
        top.appendChild(titles);
        const mark = el('span', 'mark', '');
        top.appendChild(mark);
        const send = el('button', 'send', '送出');
        top.appendChild(send);
        card.appendChild(top);

        const inner = el('div', 'card-body');
        card.appendChild(inner);
        if (step.why) inner.appendChild(el('p', 'why', step.why));
        const req = el('label', 'field');
        req.appendChild(el('span', 'field-label', '路徑'));
        const path = el('input', 'path');
        path.value = step.path;
        req.appendChild(path);
        inner.appendChild(req);

        let bodyBox = null;
        if (step.body !== undefined) {
          const f = el('label', 'field');
          f.appendChild(el('span', 'field-label', '內容'));
          bodyBox = el('textarea', 'body');
          bodyBox.value = JSON.stringify(step.body, null, 2);
          bodyBox.rows = Math.min(10, bodyBox.value.split('\n').length);
          bodyBox.spellcheck = false;
          f.appendChild(bodyBox);
          inner.appendChild(f);
        }

        const expect = el('p', 'expect');
        expect.appendChild(el('span', 'field-label', '預期'));
        expect.appendChild(el('span', '', step.expect.map(e => `HTTP ${e.status}、code = ${e.code}`).join(' 或 ')));
        inner.appendChild(expect);
        const result = el('div', 'result');
        inner.appendChild(result);
        g.appendChild(card);

        // 完成標準表的「親手試」欄
        const links = [];
        (step.covers || []).forEach(d => {
          if (!critRows[d]) return;
          const a = el('a', 'step-link', String(myNo));
          a.href = '#step-' + myNo;
          a.title = step.title;
          a.addEventListener('click', () => { card.open = true; });
          critRows[d].appendChild(a);
          links.push(a);
        });

        const entry = { state: 'idle', run, reset };
        cards.push(entry);

        function setState(state) {
          entry.state = state;
          card.dataset.state = state;
          links.forEach(a => { a.dataset.state = state; });
          mark.textContent = state === 'pass' ? '符合預期' : state === 'fail' ? '不符預期' : '';
          refreshSummary();
        }

        function reset() {
          result.replaceChildren();
          card.open = false;
          setState('idle');
        }

        async function run() {
          result.replaceChildren(el('p', 'muted', '送出中…'));
          const r = rand();
          const url = path.value.replaceAll('{{rand}}', r);
          const opts = { method: step.method, headers: {} };
          if (op.value.trim()) opts.headers['X-Operator'] = op.value.trim();
          if (bodyBox) {
            opts.headers['Content-Type'] = 'application/json';
            opts.body = bodyBox.value.replaceAll('{{rand}}', r);
          }

          let status, text, json = null;
          try {
            const res = await fetch(url, opts);
            status = res.status;
            text = await res.text();
            try { json = JSON.parse(text); } catch (e) { /* 不是 JSON 就照原文顯示 */ }
          } catch (e) {
            result.replaceChildren(el('p', 'line bad', '送不出去：' + e.message + '（服務有在跑嗎？）'));
            setState('fail');
            return;
          }

          const code = json && json.code;
          const lines = [];
          const statusOk = step.expect.some(e => e.status === status && e.code === code);
          lines.push({ ok: statusOk, label: `實際：HTTP ${status}、code = ${code}` });
          if (statusOk) {
            (step.checks || []).forEach(c => {
              let ok = false;
              try { ok = c.test(json) === true; } catch (e) { ok = false; }
              lines.push({ ok, label: c.label });
            });
          }

          const list = el('ul', 'lines');
          lines.forEach(l => {
            const li = el('li', 'line ' + (l.ok ? 'ok' : 'bad'));
            li.appendChild(el('span', 'tick', l.ok ? '✓' : '✗'));
            li.appendChild(el('span', '', l.label));
            list.appendChild(li);
          });

          const details = el('details', 'resp');
          details.appendChild(el('summary', '', '回應內容'));
          details.appendChild(el('pre', '', json ? JSON.stringify(json, null, 2) : text));
          const allOk = lines.every(l => l.ok);
          details.open = true;

          result.replaceChildren(list, details);
          setState(allOk ? 'pass' : 'fail');
          // 自己按送出就展開看回應；全部依序執行時只展開不符預期的
          card.open = !allOk || !runningAll;
        }

        // 送出鍵在收合列上：按了不要順便收合或展開
        send.addEventListener('click', e => { e.preventDefault(); run(); });
      });

      trySec.appendChild(g);
    });

    Object.values(critRows).forEach(td => {
      if (!td.children.length) td.appendChild(el('span', 'muted-text', '沒有步驟'));
    });

    runAll.addEventListener('click', async () => {
      runningAll = true;
      runAll.disabled = true;
      for (const c of cards) await c.run();
      runAll.disabled = false;
      runningAll = false;
    });
    clear.addEventListener('click', () => cards.forEach(c => c.reset()));
    refreshSummary();

    // ---- 5 改了什麼 ----
    navLink('changes', '改了什麼');
    const ch = section(main, 'changes', '改了什麼', '程式碼的 diff 請在 IDE 或 git 看，這裡是導覽。');

    if ((config.apiChanges || []).length) {
      ch.appendChild(el('h3', 'sub', '既有 API 的變動'));
      const t = table(['端點', '變了什麼']);
      config.apiChanges.forEach(a => {
        const tr = el('tr');
        tr.appendChild(el('td', 'nowrap')).appendChild(el('code', '', a.endpoint));
        rich(tr.appendChild(el('td')), a.change);
        t.body.appendChild(tr);
      });
      ch.appendChild(t.wrap);
    }

    (config.migrations || []).forEach((f, i) => {
      ch.appendChild(el('h3', 'sub', '表結構變更：db/migration/' + f));
      ch.appendChild(el('pre', 'sql', sqlTexts[i] || '讀不到這個檔（服務要用 ./dev.sh app-start 啟動）'));
    });

    (config.changes || []).forEach(group => {
      ch.appendChild(el('h3', 'sub', `${group.title}（${group.files.length} 個檔）`));
      const t = table(['檔案', '說明']);
      group.files.forEach(f => {
        const tr = el('tr');
        tr.appendChild(el('td', 'file-path')).appendChild(el('code', '', f.path));
        rich(tr.appendChild(el('td')), f.note || '');
        t.body.appendChild(tr);
      });
      ch.appendChild(t.wrap);
    });

    // ---- 6 上線與範圍 ----
    navLink('deploy', '上線與範圍');
    const dep = section(main, 'deploy', '上線與範圍');
    function bullets(title, items, empty) {
      dep.appendChild(el('h3', 'sub', title));
      if (!items.length) { dep.appendChild(el('p', 'note', empty)); return; }
      const ul = el('ul', 'plain');
      items.forEach(i => rich(ul.appendChild(el('li')), i));
      dep.appendChild(ul);
    }
    bullets('上線要注意', config.deploy || [], '沒有');
    bullets('這次不做的事（來自 requirement.md）',
      mdSection(reqText, '不做的事').filter(l => l.startsWith('- ')).map(l => l.slice(2)), '沒有');
    bullets('範圍外發現、沒動手的事', config.findings || [], '沒有');

    // ---- 7 檢查紀錄原文 ----
    navLink('records', '檢查紀錄');
    const rec = section(main, 'records', '檢查紀錄原文', '由 `./dev.sh check` 產生，頁面直接讀檔顯示。');
    [['check-green.txt（實作後）', t5Text], ['check-red.txt（實作前的紅燈）', t4Text]].forEach(([name, text]) => {
      const d = el('details', 'resp');
      d.appendChild(el('summary', '', name));
      d.appendChild(el('pre', '', text || '讀不到這個檔'));
      rec.appendChild(d);
    });

    if (location.hash) {
      const target = document.getElementById(location.hash.slice(1));
      if (target) target.scrollIntoView();
    }
  };
})();
