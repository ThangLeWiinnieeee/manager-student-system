(() => {
    const type = document.getElementById('type');
    const reviewer = document.getElementById('reviewer-deadline-field');
    const council = document.getElementById('council-date-field');
    function update() {
        for (const [field, enabled] of [
            [reviewer, type.value === 'TLCN' || type.value === 'KLTN'],
            [council, type.value === 'KLTN']
        ]) {
            field.hidden = !enabled;
            const input = field.querySelector('input');
            input.disabled = !enabled;
            input.required = enabled;
        }
    }
    type.addEventListener('change', update);
    update();
})();
