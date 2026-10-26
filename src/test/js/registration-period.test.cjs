const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');

let onChange;
const type = { value: 'MON_HOC', addEventListener: (_, callback) => { onChange = callback; } };
const reviewerInput = {}, councilInput = {};
const reviewer = { querySelector: () => reviewerInput };
const council = { querySelector: () => councilInput };
const elements = { type, 'reviewer-deadline-field': reviewer, 'council-date-field': council };
vm.runInNewContext(fs.readFileSync('src/main/resources/static/js/registration-period.js', 'utf8'), {
    document: { getElementById: id => elements[id] }
});
assert.equal(reviewer.hidden, true);
assert.equal(council.hidden, true);
for (const [value, needsReviewer, needsCouncil] of [
    ['KLTN', true, true], ['TLCN', true, false], ['NCKH', false, false], ['MON_HOC', false, false]
]) {
    type.value = value;
    onChange();
    assert.equal(reviewer.hidden, !needsReviewer);
    assert.equal(reviewerInput.required, needsReviewer);
    assert.equal(reviewerInput.disabled, !needsReviewer);
    assert.equal(council.hidden, !needsCouncil);
    assert.equal(councilInput.required, needsCouncil);
    assert.equal(councilInput.disabled, !needsCouncil);
}
console.log('Registration period deadline fields: OK');
