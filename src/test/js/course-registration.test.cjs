const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
let change;
const period = { tagName: 'SELECT', selectedOptions: [{ dataset: {} }],
    addEventListener: (_, callback) => { change = callback; } };
const name = { value: 'Lập trình web', readOnly: false };
const script = fs.readFileSync('src/main/resources/static/js/course-registration.js', 'utf8');
vm.runInNewContext(script, { document: { getElementById: id => id === 'periodId' ? period : name } });
assert.equal(name.required, true);
for (const label of ['Tiểu luận chuyên ngành', 'Khóa luận tốt nghiệp', 'Nghiên cứu khoa học']) {
    period.selectedOptions[0].dataset.courseName = label;
    change();
    assert.equal(name.value, label);
    assert.equal(name.readOnly, true);
    assert.equal(name.required, false);
}
period.selectedOptions[0].dataset.courseName = '';
change();
assert.equal(name.value, 'Lập trình web');
assert.equal(name.readOnly, false);
assert.equal(name.required, true);
period.tagName = 'INPUT';
period.dataset = { courseName: 'Nghiên cứu khoa học' };
vm.runInNewContext(script, { document: { getElementById: id => id === 'periodId' ? period : name } });
assert.equal(name.value, 'Nghiên cứu khoa học');
assert.equal(name.readOnly, true);
console.log('Course registration automatic names: OK');
