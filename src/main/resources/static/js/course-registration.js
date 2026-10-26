(() => {
    const period = document.getElementById('periodId');
    const name = document.getElementById('courseName');
    let manualName = name.value;
    function updateCourseName() {
        const selected = period.tagName === 'SELECT' ? period.selectedOptions[0] : period;
        const automaticName = selected?.dataset.courseName || '';
        if (!name.readOnly) manualName = name.value;
        name.value = automaticName || manualName;
        name.readOnly = Boolean(automaticName);
        name.required = !automaticName;
    }
    period.addEventListener('change', updateCourseName);
    updateCourseName();
})();
