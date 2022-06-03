$(document).ready(function () {
  var procid = Math.floor(Math.random() * 1000 + 1);
  $("#userspan").text(procid);
  let url = "http://localhost:8080/connect?procid=" + procid;
  var eventSource = new EventSource(url);

  eventSource.addEventListener("ServerEvent", function (event) {
    let data = JSON.parse(event.data);
    addBlock(data.title, data.text);
  });

  const button_1 = document.getElementById("post-btn-1");
  button_1.addEventListener("click", async (_) => {
    console.log(procid);
    try {
      const response = await fetch(
        "http://localhost:8080/reqrecCOCA?procid=" + procid,
        {
          method: "POST",
          body: {
            procid: procid,
          },
        }
      );
      console.log("Completed!", response);
    } catch (err) {
      console.error(`Error: ${err}`);
    }
  });

  const button_2 = document.getElementById("post-btn-2");
  button_2.addEventListener("click", async (_) => {
    console.log(procid);
    try {
      const response = await fetch(
        "http://localhost:8080/reqrecPEPSI?procid=" + procid,
        {
          method: "POST",
          body: {
            procid: procid,
          },
        }
      );
      console.log("Completed!", response);
    } catch (err) {
      console.error(`Error: ${err}`);
    }
  });

  const button_3 = document.getElementById("post-btn-3");
  button_3.addEventListener("click", async (_) => {
    console.log(procid);
    try {
      const response = await fetch(
        "http://localhost:8080/freerecCOCA?procid=" + procid,
        {
          method: "POST",
          body: {
            procid: procid,
          },
        }
      );
      console.log("Completed!", response);
    } catch (err) {
      console.error(`Error: ${err}`);
    }
  });

  const button_4 = document.getElementById("post-btn-4");
  button_4.addEventListener("click", async (_) => {
    console.log(procid);
    try {
      const response = await fetch(
        "http://localhost:8080/freerecPEPSI?procid=" + procid,
        {
          method: "POST",
          body: {
            procid: procid,
          },
        }
      );
      console.log("Completed!", response);
    } catch (err) {
      console.error(`Error: ${err}`);
    }
  });
});

function addBlock(title, text) {
  let a = document.createElement("article");
  // title
  let h3 = document.createElement("h3");
  let t = document.createTextNode(title);
  h3.appendChild(t);
  // Event Text
  let p = document.createElement("p");
  p.innerHTML = text;
  a.appendChild(h3);
  a.appendChild(p);

  document.getElementById("pack").appendChild(a);
}
