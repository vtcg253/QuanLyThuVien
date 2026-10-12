(() => {
    const body = document.body;
    const toggle = document.querySelector(".sidebar-toggle");
    const backdrop = document.querySelector(".sidebar-backdrop");
    const mobile = window.matchMedia("(max-width: 800px)");

    document.querySelectorAll(".alert").forEach((alert) => {
        const isError = alert.classList.contains("alert-err");
        alert.setAttribute("role", isError ? "alert" : "status");
        alert.setAttribute("aria-live", isError ? "assertive" : "polite");
        window.setTimeout(() => {
            alert.classList.add("is-dismissing");
            window.setTimeout(() => alert.remove(), 250);
        }, 5000);
    });

    if (!toggle || !backdrop) {
        return;
    }

    const setState = (open) => {
        body.classList.toggle("sidebar-open", mobile.matches && open);
        body.classList.toggle("sidebar-collapsed", !mobile.matches && !open);
        toggle.setAttribute("aria-expanded", String(open));
        toggle.setAttribute("aria-label", open ? "Thu gọn thanh điều hướng" : "Mở rộng thanh điều hướng");
        if (!mobile.matches) {
            localStorage.setItem("library-sidebar-open", String(open));
        }
    };

    const savedState = localStorage.getItem("library-sidebar-open");
    setState(mobile.matches ? false : savedState !== "false");

    const readerToggle = document.querySelector(".reader-toggle");
    const readerPanel = document.querySelector("#add-reader-panel");
    const readerNameInput = document.querySelector("#new-reader-name");
    const readerBirthdateInput = document.querySelector("#new-reader-birthdate");
    const readerPhoneInput = document.querySelector("#new-reader-phone");
    const readerAddressInput = document.querySelector("#new-reader-address");
    const readerButton = document.querySelector("#add-reader-button");
    const readerMessage = document.querySelector("#reader-add-message");
    const readerSelect = document.querySelector("#maDocGia");

    if (readerToggle && readerPanel && readerNameInput && readerBirthdateInput
        && readerPhoneInput && readerAddressInput && readerButton && readerMessage && readerSelect) {
        readerToggle.addEventListener("click", () => {
            readerPanel.showModal();
            readerNameInput.focus();
        });

        readerPanel.querySelector("[data-close-reader-dialog]")?.addEventListener("click", () => readerPanel.close());
        readerPanel.addEventListener("click", (event) => {
            if (event.target === readerPanel) {
                readerPanel.close();
            }
        });

        readerButton.addEventListener("click", async () => {
            const hoTen = readerNameInput.value.trim();
            readerMessage.textContent = "";
            readerMessage.classList.remove("error");
            if (!hoTen) {
                readerMessage.textContent = "Vui lòng nhập họ tên độc giả.";
                readerMessage.classList.add("error");
                readerNameInput.focus();
                return;
            }

            readerButton.disabled = true;
            try {
                const readerDetails = new URLSearchParams({
                    hoTen,
                    dienThoai: readerPhoneInput.value.trim(),
                    diaChi: readerAddressInput.value.trim()
                });
                if (readerBirthdateInput.value) {
                    readerDetails.set("ngaySinh", readerBirthdateInput.value);
                }
                const response = await fetch("/muontra/docgia", {
                    method: "POST",
                    headers: csrfHeaders(),
                    body: readerDetails
                });
                const result = await response.json();
                if (!response.ok) {
                    readerMessage.textContent = result.error || "Không thể thêm độc giả.";
                    readerMessage.classList.add("error");
                    return;
                }

                const option = document.createElement("option");
                option.value = result.maDocGia;
                option.textContent = `${result.maDocGia} - ${result.hoTen}`;
                readerSelect.add(option);
                readerSelect.value = result.maDocGia;
                readerNameInput.value = "";
                readerBirthdateInput.value = "";
                readerPhoneInput.value = "";
                readerAddressInput.value = "";
                readerPanel.close();
            } catch (error) {
                readerMessage.textContent = "Không kết nối được máy chủ. Vui lòng thử lại.";
                readerMessage.classList.add("error");
            } finally {
                readerButton.disabled = false;
            }
        });
    }

    const csrfHeaders = () => {
        const token = document.querySelector("input[name='_csrf']")?.value;
        return token
            ? { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8", "X-CSRF-TOKEN": token }
            : { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" };
    };

    const getSearchInput = (field) => Array.from(document.querySelectorAll("[data-search-field]"))
        .find((input) => input.dataset.searchField === field.id);

    const syncSearchInput = (field, selectedValue = field.value) => {
        const searchInput = getSearchInput(field);
        if (!(searchInput instanceof HTMLInputElement)) {
            return;
        }
        const datalist = searchInput.list;
        const option = datalist
            ? Array.from(datalist.options).find((item) => item.dataset.code === selectedValue)
            : null;
        searchInput.value = option?.value || "";
    };

    const addSearchOption = (field, code, label) => {
        const searchInput = getSearchInput(field);
        const datalist = searchInput instanceof HTMLInputElement ? searchInput.list : null;
        if (!datalist) {
            return;
        }
        let option = Array.from(datalist.options).find((item) => item.dataset.code === code);
        if (!option) {
            option = new Option(label);
            option.dataset.code = code;
            datalist.add(option);
        } else {
            option.value = label;
        }
        field.value = code;
        syncSearchInput(field, code);
    };

    document.querySelectorAll("[data-search-field]").forEach((searchInput) => {
        if (!(searchInput instanceof HTMLInputElement)) {
            return;
        }
        const field = document.getElementById(searchInput.dataset.searchField || "");
        if (!(field instanceof HTMLInputElement)) {
            return;
        }

        syncSearchInput(field);
        searchInput.addEventListener("input", () => {
            const query = searchInput.value.trim().toLocaleLowerCase();
            const match = searchInput.list
                ? Array.from(searchInput.list.options).find((option) =>
                    option.value.trim().toLocaleLowerCase() === query)
                : null;
            field.value = match?.dataset.code || "";
        });
    });

    const loanBookRows = document.querySelector("#loan-book-rows");
    const addLoanBookButton = document.querySelector("#add-loan-book");
    if (loanBookRows instanceof HTMLTableSectionElement && addLoanBookButton) {
        const reindexLoanBookRows = () => {
            loanBookRows.querySelectorAll("tr").forEach((row, index) => {
                const select = row.querySelector("[data-loan-book-select]");
                const quantity = row.querySelector("[data-loan-book-quantity]");
                if (select instanceof HTMLSelectElement) {
                    select.name = `chiTiet[${index}].maSach`;
                    select.id = `chiTiet${index}.maSach`;
                }
                if (quantity instanceof HTMLInputElement) {
                    quantity.name = `chiTiet[${index}].soLuong`;
                    quantity.id = `chiTiet${index}.soLuong`;
                }
                const removeButton = row.querySelector(".loan-book-remove");
                if (removeButton instanceof HTMLButtonElement) {
                    removeButton.disabled = loanBookRows.rows.length === 1;
                }
            });
        };

        addLoanBookButton.addEventListener("click", () => {
            const firstRow = loanBookRows.querySelector("tr");
            if (!firstRow) {
                return;
            }
            const newRow = firstRow.cloneNode(true);
            const select = newRow.querySelector("[data-loan-book-select]");
            const quantity = newRow.querySelector("[data-loan-book-quantity]");
            if (select instanceof HTMLSelectElement) {
                select.value = "";
            }
            if (quantity instanceof HTMLInputElement) {
                quantity.value = "1";
            }
            loanBookRows.append(newRow);
            reindexLoanBookRows();
            select?.focus();
        });

        loanBookRows.addEventListener("click", (event) => {
            const target = event.target;
            if (!(target instanceof Element) || !target.closest(".loan-book-remove")) {
                return;
            }
            const row = target.closest("tr");
            if (loanBookRows.rows.length > 1) {
                row?.remove();
                reindexLoanBookRows();
            }
        });

        reindexLoanBookRows();
    }

    toggle.addEventListener("click", () => {
        const open = mobile.matches
            ? !body.classList.contains("sidebar-open")
            : body.classList.contains("sidebar-collapsed");
        setState(open);
    });

    backdrop.addEventListener("click", () => setState(false));
    mobile.addEventListener("change", () => {
        const shouldOpen = !mobile.matches && localStorage.getItem("library-sidebar-open") !== "false";
        setState(shouldOpen);
    });

    const authorToggle = document.querySelector(".author-toggle");
    const authorPanel = document.querySelector("#add-author-panel");
    const authorInput = document.querySelector("#new-author-name");
    const authorAddressInput = document.querySelector("#new-author-address");
    const authorButton = document.querySelector("#add-author-button");
    const authorMessage = document.querySelector("#author-add-message");
    const authorField = document.querySelector("#maTacGia");

    if (authorToggle && authorPanel && authorInput && authorAddressInput && authorButton && authorMessage
        && authorField instanceof HTMLInputElement) {
        authorToggle.addEventListener("click", () => {
            authorPanel.showModal();
            authorToggle.setAttribute("aria-expanded", "true");
            authorInput.focus();
        });

        authorPanel.querySelector("[data-close-author-dialog]")?.addEventListener("click", () => authorPanel.close());
        authorPanel.addEventListener("close", () => authorToggle.setAttribute("aria-expanded", "false"));
        authorPanel.addEventListener("click", (event) => {
            if (event.target === authorPanel) {
                authorPanel.close();
            }
        });

        authorButton.addEventListener("click", async () => {
            const tenTacGia = authorInput.value.trim();
            authorMessage.textContent = "";
            authorMessage.classList.remove("error");
            if (!tenTacGia) {
                authorMessage.textContent = "Vui lòng nhập tên tác giả.";
                authorMessage.classList.add("error");
                authorInput.focus();
                return;
            }

            authorButton.disabled = true;
            try {
                const response = await fetch("/sach/tac-gia", {
                    method: "POST",
                    headers: csrfHeaders(),
                    body: new URLSearchParams({
                        tenTacGia,
                        diaChi: authorAddressInput.value.trim()
                    })
                });
                const result = await response.json();
                if (!response.ok) {
                    authorMessage.textContent = result.error || "Không thể thêm tác giả.";
                    authorMessage.classList.add("error");
                    return;
                }

                addSearchOption(authorField, result.maTacGia, `${result.maTacGia} - ${result.tenTacGia}`);
                authorInput.value = "";
                authorAddressInput.value = "";
                authorMessage.textContent = `Đã thêm ${result.tenTacGia} (${result.maTacGia}).`;
                authorPanel.close();
            } catch (error) {
                authorMessage.textContent = "Không kết nối được máy chủ. Vui lòng thử lại.";
                authorMessage.classList.add("error");
            } finally {
                authorButton.disabled = false;
            }
        });
    }

    const categoryToggle = document.querySelector(".category-toggle");
    const categoryPanel = document.querySelector("#add-category-panel");
    const categoryInput = document.querySelector("#new-category-name");
    const categoryButton = document.querySelector("#add-category-button");
    const categoryMessage = document.querySelector("#category-add-message");
    const categoryField = document.querySelector("#maTheLoai");

    if (categoryToggle && categoryPanel && categoryInput && categoryButton && categoryMessage
        && categoryField instanceof HTMLInputElement) {
        categoryToggle.addEventListener("click", () => {
            categoryPanel.showModal();
            categoryToggle.setAttribute("aria-expanded", "true");
            categoryInput.focus();
        });

        categoryPanel.querySelector("[data-close-category-dialog]")?.addEventListener("click", () => categoryPanel.close());
        categoryPanel.addEventListener("close", () => categoryToggle.setAttribute("aria-expanded", "false"));
        categoryPanel.addEventListener("click", (event) => {
            if (event.target === categoryPanel) {
                categoryPanel.close();
            }
        });

        categoryButton.addEventListener("click", async () => {
            const tenTheLoai = categoryInput.value.trim();
            categoryMessage.textContent = "";
            categoryMessage.classList.remove("error");
            if (!tenTheLoai) {
                categoryMessage.textContent = "Vui lòng nhập tên thể loại.";
                categoryMessage.classList.add("error");
                categoryInput.focus();
                return;
            }

            categoryButton.disabled = true;
            try {
                const response = await fetch("/sach/the-loai", {
                    method: "POST",
                    headers: csrfHeaders(),
                    body: new URLSearchParams({ tenTheLoai })
                });
                const result = await response.json();
                if (!response.ok) {
                    categoryMessage.textContent = result.error || "Không thể thêm thể loại.";
                    categoryMessage.classList.add("error");
                    return;
                }

                addSearchOption(categoryField, result.maTheLoai, `${result.maTheLoai} - ${result.tenTheLoai}`);
                categoryInput.value = "";
                categoryMessage.textContent = `Đã thêm ${result.tenTheLoai} (${result.maTheLoai}).`;
                categoryPanel.close();
            } catch (error) {
                categoryMessage.textContent = "Không kết nối được máy chủ. Vui lòng thử lại.";
                categoryMessage.classList.add("error");
            } finally {
                categoryButton.disabled = false;
            }
        });
    }

    const publisherToggle = document.querySelector(".publisher-toggle");
    const publisherPanel = document.querySelector("#add-publisher-panel");
    const publisherNameInput = document.querySelector("#new-publisher-name");
    const publisherAddressInput = document.querySelector("#new-publisher-address");
    const publisherPhoneInput = document.querySelector("#new-publisher-phone");
    const publisherButton = document.querySelector("#add-publisher-button");
    const publisherMessage = document.querySelector("#publisher-add-message");
    const publisherField = document.querySelector("#maNxb");

    if (publisherToggle && publisherPanel && publisherNameInput && publisherAddressInput
        && publisherPhoneInput && publisherButton && publisherMessage
        && publisherField instanceof HTMLInputElement) {
        publisherToggle.addEventListener("click", () => {
            publisherPanel.showModal();
            publisherNameInput.focus();
        });

        publisherPanel.querySelector("[data-close-publisher-dialog]")?.addEventListener("click", () => publisherPanel.close());
        publisherPanel.addEventListener("click", (event) => {
            if (event.target === publisherPanel) {
                publisherPanel.close();
            }
        });

        publisherButton.addEventListener("click", async () => {
            const tenNxb = publisherNameInput.value.trim();
            publisherMessage.textContent = "";
            publisherMessage.classList.remove("error");
            if (!tenNxb) {
                publisherMessage.textContent = "Vui lòng nhập tên nhà xuất bản.";
                publisherMessage.classList.add("error");
                publisherNameInput.focus();
                return;
            }

            publisherButton.disabled = true;
            try {
                const response = await fetch("/sach/nha-xuat-ban", {
                    method: "POST",
                    headers: csrfHeaders(),
                    body: new URLSearchParams({
                        tenNxb,
                        diaChi: publisherAddressInput.value.trim(),
                        sdt: publisherPhoneInput.value.trim()
                    })
                });
                const result = await response.json();
                if (!response.ok) {
                    publisherMessage.textContent = result.error || "Không thể thêm nhà xuất bản.";
                    publisherMessage.classList.add("error");
                    return;
                }

                addSearchOption(publisherField, result.maNxb, `${result.maNxb} - ${result.tenNxb}`);
                publisherNameInput.value = "";
                publisherAddressInput.value = "";
                publisherPhoneInput.value = "";
                publisherMessage.textContent = `Đã thêm ${result.tenNxb} (${result.maNxb}).`;
                publisherPanel.close();
            } catch (error) {
                publisherMessage.textContent = "Không kết nối được máy chủ. Vui lòng thử lại.";
                publisherMessage.classList.add("error");
            } finally {
                publisherButton.disabled = false;
            }
        });
    }

    document.querySelectorAll("[data-open-loan-detail]").forEach((button) => {
        const dialogId = button.dataset.openLoanDetail;
        const dialog = dialogId ? document.getElementById(dialogId) : null;
        if (!(dialog instanceof HTMLDialogElement)) {
            return;
        }

        button.addEventListener("click", () => dialog.showModal());
        dialog.querySelectorAll("[data-close-loan-detail]").forEach((closeButton) => {
            closeButton.addEventListener("click", () => dialog.close());
        });
        dialog.addEventListener("click", (event) => {
            if (event.target === dialog) {
                dialog.close();
            }
        });
    });

    const categoryDialog = document.querySelector("#category-dialog");
    if (categoryDialog instanceof HTMLDialogElement) {
        document.querySelector("[data-open-category-dialog]")?.addEventListener("click", () => {
            categoryDialog.showModal();
            categoryDialog.querySelector("input[name='ten']")?.focus();
        });

        categoryDialog.querySelectorAll("[data-close-category-dialog]").forEach((button) => {
            button.addEventListener("click", () => categoryDialog.close());
        });

        categoryDialog.addEventListener("click", (event) => {
            if (event.target === categoryDialog) {
                categoryDialog.close();
            }
        });

        if (categoryDialog.dataset.openOnLoad === "true") {
            categoryDialog.showModal();
            categoryDialog.querySelector("input[name='ten']")?.focus();
        }
    }

    const actionConfirmDialog = document.querySelector("#action-confirm-dialog");
    const actionConfirmMessage = document.querySelector("#action-confirm-message");
    const confirmActionButton = document.querySelector("#confirm-action");
    if (actionConfirmDialog instanceof HTMLDialogElement
        && actionConfirmMessage instanceof HTMLElement
        && confirmActionButton instanceof HTMLButtonElement) {
        let pendingForm = null;
        let pendingSubmitter = null;

        document.querySelectorAll("form[data-confirm]").forEach((form) => {
            form.addEventListener("submit", (event) => {
                if (form.dataset.confirmed === "true") {
                    delete form.dataset.confirmed;
                    return;
                }

                event.preventDefault();
                pendingForm = form;
                pendingSubmitter = event.submitter;
                actionConfirmMessage.textContent = form.dataset.confirm || "";
                actionConfirmDialog.showModal();
            });
        });

        actionConfirmDialog.querySelectorAll("[data-close-action-confirm]").forEach((button) => {
            button.addEventListener("click", () => {
                pendingForm = null;
                pendingSubmitter = null;
                actionConfirmDialog.close();
            });
        });

        actionConfirmDialog.addEventListener("click", (event) => {
            if (event.target === actionConfirmDialog) {
                pendingForm = null;
                pendingSubmitter = null;
                actionConfirmDialog.close();
            }
        });

        confirmActionButton.addEventListener("click", () => {
            const form = pendingForm;
            const submitter = pendingSubmitter;
            pendingForm = null;
            pendingSubmitter = null;
            actionConfirmDialog.close();

            if (!form || !form.reportValidity()) {
                return;
            }

            form.dataset.confirmed = "true";
            if (submitter instanceof HTMLElement) {
                form.requestSubmit(submitter);
            } else {
                form.requestSubmit();
            }
        });
    }

    const deleteDialog = document.querySelector("#delete-dialog");
    const deleteName = document.querySelector("#delete-item-name");
    const confirmDeleteButton = document.querySelector("#confirm-delete-item");
    if (deleteDialog instanceof HTMLDialogElement && deleteName && confirmDeleteButton) {
        let pendingDeleteForm = null;

        document.querySelectorAll(".confirm-delete-form").forEach((form) => {
            form.addEventListener("submit", (event) => {
                event.preventDefault();
                pendingDeleteForm = form;
                deleteName.textContent = form.dataset.confirmName || "thông tin này";
                deleteDialog.showModal();
            });
        });

        deleteDialog.querySelectorAll("[data-close-delete-dialog]").forEach((button) => {
            button.addEventListener("click", () => {
                pendingDeleteForm = null;
                deleteDialog.close();
            });
        });

        deleteDialog.addEventListener("click", (event) => {
            if (event.target === deleteDialog) {
                pendingDeleteForm = null;
                deleteDialog.close();
            }
        });

        confirmDeleteButton.addEventListener("click", () => {
            if (pendingDeleteForm) {
                pendingDeleteForm.submit();
            }
        });
    }
})();
